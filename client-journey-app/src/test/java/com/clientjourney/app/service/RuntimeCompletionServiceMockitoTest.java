package com.clientjourney.app.service;

import com.clientjourney.app.dto.OutputMessage;
import com.clientjourney.app.dto.SelectedServiceDto;
import com.clientjourney.visit.spi.VisitCreationClient;
import com.clientjourney.visit.spi.VisitCreationResult;
import com.clientjourney.visit.spi.VisitCreationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RuntimeCompletionServiceMockitoTest {

    @Test
    void shouldCreateVisitUsingMocks() {
        ServiceSelectionProcessor selectionProcessor = mock(ServiceSelectionProcessor.class);
        ConversationSessionService sessionService = mock(ConversationSessionService.class);
        VisitCreationClient visitClient = mock(VisitCreationClient.class);

        UUID sessionId = UUID.randomUUID();
        when(selectionProcessor.processSelectedServices(eq(sessionId), any())).thenReturn(OutputMessage.result("ok"));
        when(sessionService.findSession(sessionId)).thenReturn(Optional.of(new ConversationSessionService.SessionState(
            "scenario-1", "web", "user-1", Map.of("lang", "ru"), List.of(new SelectedServiceDto("1", "svc", "S")), 1, 3, List.of()
        )));
        when(visitClient.createVisit(any())).thenReturn(new VisitCreationResult(VisitCreationStatus.SUCCESS, "MOCK", "v1", "t1", null, null));

        RuntimeCompletionService service = new RuntimeCompletionService(selectionProcessor, sessionService, visitClient, new VisitCreationSettingsService());
        var response = service.selectServicesAndComplete(sessionId, List.of(new SelectedServiceDto("1", "svc", "S")));

        assertEquals("COMPLETED", response.status());
        assertEquals("MOCK", response.visitCreation().clientType());
        verify(visitClient, times(1)).createVisit(any());
    }
}
