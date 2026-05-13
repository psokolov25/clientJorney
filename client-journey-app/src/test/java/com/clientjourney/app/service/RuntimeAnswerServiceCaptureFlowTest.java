package com.clientjourney.app.service;

import com.clientjourney.app.dto.AnswerRequest;
import com.clientjourney.app.repository.InMemoryScenarioGraphRepository;
import com.clientjourney.app.repository.InMemoryScenarioRepository;
import com.clientjourney.core.ScenarioEngine;
import com.clientjourney.domain.model.AnswerOption;
import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioEdge;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.ScenarioNode;
import com.clientjourney.domain.model.ScenarioStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuntimeAnswerServiceCaptureFlowTest {

    @Test
    void shouldPropagateNextInputFromCaptureResultToSessionMetadata() {
        ConversationSessionService sessionService = new ConversationSessionService();
        InMemoryScenarioRepository scenarioRepository = new InMemoryScenarioRepository();
        InMemoryScenarioGraphRepository graphRepository = new InMemoryScenarioGraphRepository();

        UUID scenarioId = UUID.randomUUID();
        scenarioRepository.save(new Scenario(scenarioId, "capture-scenario", "Capture", null, ScenarioStatus.DRAFT, 1, Instant.now(), Instant.now()));
        graphRepository.save(new ScenarioGraph(
            scenarioId,
            1,
            List.of(
                new ScenarioNode("start", NodeType.START, "start", "Start", List.of(), List.of()),
                new ScenarioNode("capture", NodeType.GROOVY_CAPTURE, "return [__nextInput: 'id-777', payload: answerValue]", "Capture", List.of(new AnswerOption("ok", "ok", "OK", "end")), List.of()),
                new ScenarioNode("end", NodeType.RESULT, "end", "Done", List.of(), List.of())
            ),
            List.of(
                new ScenarioEdge("e1", "start", "next", "capture"),
                new ScenarioEdge("e2", "capture", "ok", "end")
            )
        ));

        RuntimeAnswerService service = new RuntimeAnswerService(
            sessionService,
            new ScenarioEngine(),
            scenarioRepository,
            graphRepository,
            new CaptureNodeProcessingService(null)
        );

        UUID sessionId = UUID.randomUUID();
        sessionService.registerSession(
            sessionId,
            "capture-scenario",
            "WEB",
            "user-1",
            Map.of("currentNodeId", "capture"),
            List.of(),
            1,
            3
        );

        service.processAnswer(sessionId, new AnswerRequest("ok", "photo-content"));

        var state = sessionService.findSession(sessionId).orElseThrow();
        assertEquals("end", state.metadata().get("currentNodeId"));
        assertEquals("id-777", state.metadata().get("capture.nextInput"));
    }
}
