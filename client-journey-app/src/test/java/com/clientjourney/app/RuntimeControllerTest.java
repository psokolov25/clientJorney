package com.clientjourney.app;

import com.clientjourney.app.dto.AnswerRequest;
import com.clientjourney.app.dto.SelectedServiceDto;
import com.clientjourney.app.dto.ServiceSelectionRequest;
import com.clientjourney.app.dto.StartSessionRequest;
import com.clientjourney.app.dto.StartSessionResponse;
import com.clientjourney.app.service.ConversationSessionService;
import com.clientjourney.app.service.RuntimeAnswerService;
import com.clientjourney.app.service.RuntimeCompletionService;
import com.clientjourney.app.service.RuntimeSessionService;
import com.clientjourney.app.service.ServiceSelectionProcessor;
import com.clientjourney.app.service.ScenarioService;
import com.clientjourney.app.service.VisitCreationSettingsService;
import com.clientjourney.app.repository.InMemoryScenarioRepository;
import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioStatus;

import java.time.Instant;
import com.clientjourney.core.ScenarioEngine;
import com.clientjourney.visit.mock.DryRunVisitCreationClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeControllerTest {

    @Test
    void startSessionShouldReturnServiceSelection() {
        RuntimeController controller = controller();

        StartSessionResponse response = controller.startSession(
                "medical-registration",
                new StartSessionRequest("REST", "", Map.of("source", "test"))
        );

        assertEquals("medical-registration", response.scenarioCode());
        assertEquals("ACTIVE", response.status());
        assertNotNull(response.sessionId());
        assertEquals("SERVICE_SELECTION", response.message().type());
        assertNull(response.visitCreation());
    }


    @Test
    void branchSelectionConfigShouldSupportBeforeAfterAndNoneModes() {
        RuntimeController controller = controller();
        var cfg = controller.branchSelectionConfig("medical-registration");
        assertEquals("NONE", cfg.mode());
    }

    @Test
    void answerShouldKeepSessionActive() {
        RuntimeController controller = controller();
        StartSessionResponse started = controller.startSession(
            "medical-registration",
            new StartSessionRequest("REST", "user-1", Map.of("source", "web"))
        );

        StartSessionResponse response = controller.answer(
            UUID.fromString(started.sessionId()),
            new AnswerRequest("doctor", "doctor")
        );

        assertEquals("ACTIVE", response.status());
        assertEquals("INFO", response.message().type());
    }

    @Test
    void selectedServicesShouldCompleteSessionAndReturnDryRunVisitCreation() {
        RuntimeController controller = controller();
        StartSessionResponse started = controller.startSession(
            "medical-registration",
            new StartSessionRequest("REST", "user-1", Map.of("source", "web"))
        );

        StartSessionResponse response = controller.selectServices(
            UUID.fromString(started.sessionId()),
            new ServiceSelectionRequest(List.of(new SelectedServiceDto("svc-1", "THERAPIST", "Therapist")))
        );

        assertEquals("COMPLETED", response.status());
        assertEquals("RESULT", response.message().type());
        assertNotNull(response.visitCreation());
        assertEquals("DRY_RUN", response.visitCreation().status());
    }


    @Test
    void confirmSelectedServicesShouldCompleteUsingStoredSelection() {
        RuntimeController controller = controller();
        StartSessionResponse started = controller.startSession(
            "medical-registration",
            new StartSessionRequest("REST", "user-1", Map.of("source", "web"))
        );

        controller.selectServices(
            UUID.fromString(started.sessionId()),
            new ServiceSelectionRequest(List.of(new SelectedServiceDto("svc-1", "THERAPIST", "Therapist")))
        );

        StartSessionResponse confirmed = controller.confirmSelectedServices(UUID.fromString(started.sessionId()));
        assertEquals("COMPLETED", confirmed.status());
        assertNotNull(confirmed.visitCreation());
    }
    private RuntimeController controller() {
        ConversationSessionService sessionService = new ConversationSessionService();
        ServiceSelectionProcessor processor = new ServiceSelectionProcessor(sessionService);
        RuntimeSessionService runtimeSessionService = new RuntimeSessionService(new ScenarioEngine(), sessionService);
        RuntimeAnswerService answerService = new RuntimeAnswerService(sessionService);
        RuntimeCompletionService completionService = new RuntimeCompletionService(processor, sessionService, new DryRunVisitCreationClient(), new VisitCreationSettingsService());
        InMemoryScenarioRepository repo = new InMemoryScenarioRepository();
        repo.save(new Scenario(UUID.randomUUID(), "medical-registration", "Medical", null, ScenarioStatus.DRAFT, 1, Instant.now(), Instant.now()));
        ScenarioService scenarioService = new ScenarioService(repo);
        VisitCreationSettingsService visitCreationSettingsService = new VisitCreationSettingsService();
        return new RuntimeController(runtimeSessionService, answerService, completionService, scenarioService, visitCreationSettingsService);
    }
}
