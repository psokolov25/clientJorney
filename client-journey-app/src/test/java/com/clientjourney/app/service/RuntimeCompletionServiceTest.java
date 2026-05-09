package com.clientjourney.app.service;

import com.clientjourney.app.dto.SelectedServiceDto;
import com.clientjourney.visit.spi.VisitCreationClient;
import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationResult;
import com.clientjourney.visit.spi.VisitCreationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeCompletionServiceTest {

    @Test
    void shouldUseSessionContextWhenCreatingVisit() {
        ConversationSessionService sessionService = new ConversationSessionService();
        ServiceSelectionProcessor selectionProcessor = new ServiceSelectionProcessor(sessionService);
        CapturingVisitCreationClient visitClient = new CapturingVisitCreationClient();
        RuntimeCompletionService completionService = new RuntimeCompletionService(selectionProcessor, sessionService, visitClient);

        UUID sessionId = UUID.randomUUID();
        SelectedServiceDto selected = new SelectedServiceDto("svc-1", "S1", "Service 1");
        sessionService.registerSession(sessionId, "scenario-42", "TELEGRAM", "user-abc", java.util.Map.of("source", "tg", "priority", 3), List.of(selected), 1, 3);

        var response = completionService.selectServicesAndComplete(sessionId, List.of(selected));

        assertEquals("COMPLETED", response.status());
        assertNotNull(response.visitCreation());
        assertEquals("scenario-42", visitClient.capturedRequest.scenarioCode());
        assertEquals("TELEGRAM", visitClient.capturedRequest.channel());
        assertEquals("user-abc", visitClient.capturedRequest.externalUserId());
        assertEquals("tg", visitClient.capturedRequest.parameters().get("source"));
        assertEquals("3", visitClient.capturedRequest.parameters().get("priority"));
        assertEquals("1", visitClient.capturedRequest.parameters().get("selectedServicesCount"));
    }

    private static final class CapturingVisitCreationClient implements VisitCreationClient {
        private VisitCreationRequest capturedRequest;

        @Override
        public VisitCreationResult createVisit(VisitCreationRequest request) {
            this.capturedRequest = request;
            return new VisitCreationResult(VisitCreationStatus.SUCCESS, "TEST", "visit-1", "A-1", null, null);
        }
    }
}
