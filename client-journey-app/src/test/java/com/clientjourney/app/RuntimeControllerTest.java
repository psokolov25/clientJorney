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
    void answerShouldKeepSessionActive() {
        RuntimeController controller = controller();
        StartSessionResponse started = controller.startSession(
            "medical-registration",
            new StartSessionRequest("REST", "user-1", Map.of())
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
            new StartSessionRequest("REST", "user-1", Map.of())
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

    private RuntimeController controller() {
        ConversationSessionService sessionService = new ConversationSessionService();
        ServiceSelectionProcessor processor = new ServiceSelectionProcessor(sessionService);
        RuntimeSessionService runtimeSessionService = new RuntimeSessionService(new ScenarioEngine(), sessionService);
        RuntimeAnswerService answerService = new RuntimeAnswerService(sessionService);
        RuntimeCompletionService completionService = new RuntimeCompletionService(processor, new DryRunVisitCreationClient());
        return new RuntimeController(runtimeSessionService, answerService, completionService);
    }
}
