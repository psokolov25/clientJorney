package com.clientjourney.app;

import com.clientjourney.app.dto.RuntimeWsRequest;
import com.clientjourney.app.service.ConversationSessionService;
import com.clientjourney.app.service.RuntimeAnswerService;
import com.clientjourney.app.service.RuntimeCompletionService;
import com.clientjourney.app.service.RuntimeSessionService;
import com.clientjourney.app.service.ServiceSelectionProcessor;
import com.clientjourney.visit.mock.DryRunVisitCreationClient;
import com.clientjourney.core.ScenarioEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeWebSocketTest {

    @Test
    void shouldStartSessionViaWebSocketAction() {
        ConversationSessionService sessionService = new ConversationSessionService();
        RuntimeWebSocket socket = new RuntimeWebSocket(
            new RuntimeSessionService(new ScenarioEngine(), sessionService),
            new RuntimeAnswerService(sessionService),
            new RuntimeCompletionService(new ServiceSelectionProcessor(sessionService), sessionService, new DryRunVisitCreationClient()),
            new ObjectMapper()
        );

        var response = socket.handle(new RuntimeWsRequest(
            "startSession",
            "medical-registration",
            null,
            null,
            null,
            null,
            "user-ws",
            "WS",
            Map.of("source", "widget")
        ));

        assertEquals("ACTIVE", response.status());
        assertNotNull(response.sessionId());
        var state = sessionService.findSession(UUID.fromString(response.sessionId())).orElseThrow();
        assertEquals("WS", state.channel());
        assertEquals("widget", state.metadata().get("source"));
    }

    @Test
    void shouldRejectUnknownAction() {
        ConversationSessionService sessionService = new ConversationSessionService();
        RuntimeWebSocket socket = new RuntimeWebSocket(
            new RuntimeSessionService(new ScenarioEngine(), sessionService),
            new RuntimeAnswerService(sessionService),
            new RuntimeCompletionService(new ServiceSelectionProcessor(sessionService), sessionService, new DryRunVisitCreationClient()),
            new ObjectMapper()
        );

        var ex = assertThrows(IllegalArgumentException.class,
            () -> socket.handle(new RuntimeWsRequest("ping", null, null, null, null, null, null, null, null)));
        assertTrue(ex.getMessage().contains("Unknown action"));
    }
}
