package com.clientjourney.app.service;

import com.clientjourney.app.dto.OutputMessage;
import com.clientjourney.app.dto.SelectedServiceDto;
import com.clientjourney.app.dto.StartSessionResponse;
import com.clientjourney.app.dto.VisitCreationInfo;
import com.clientjourney.visit.spi.VisitCreationClient;
import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationResult;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class RuntimeCompletionService {
    private final ServiceSelectionProcessor serviceSelectionProcessor;
    private final ConversationSessionService conversationSessionService;
    private final VisitCreationClient visitCreationClient;

    public RuntimeCompletionService(
        ServiceSelectionProcessor serviceSelectionProcessor,
        ConversationSessionService conversationSessionService,
        VisitCreationClient visitCreationClient
    ) {
        this.serviceSelectionProcessor = serviceSelectionProcessor;
        this.conversationSessionService = conversationSessionService;
        this.visitCreationClient = visitCreationClient;
    }


    public StartSessionResponse confirmSelectedServices(UUID sessionId) {
        ConversationSessionService.SessionState sessionState = conversationSessionService.findSession(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        return selectServicesAndComplete(sessionId, sessionState.selectedServices());
    }

    public StartSessionResponse selectServicesAndComplete(UUID sessionId, List<SelectedServiceDto> selectedServices) {
        OutputMessage outputMessage = serviceSelectionProcessor.processSelectedServices(sessionId, selectedServices);
        ConversationSessionService.SessionState sessionState = conversationSessionService.findSession(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        Map<String, String> parameters = new HashMap<>();
        sessionState.metadata().forEach((key, value) -> parameters.put(key, String.valueOf(value)));
        parameters.put("selectedServicesCount", String.valueOf(selectedServices == null ? 0 : selectedServices.size()));

        VisitCreationResult visit = visitCreationClient.createVisit(new VisitCreationRequest(
            sessionId,
            sessionState.scenarioCode(),
            1,
            sessionState.channel(),
            sessionState.externalUserId(),
            parameters
        ));

        VisitCreationInfo visitInfo = new VisitCreationInfo(
            visit.status().name(),
            visit.clientType(),
            visit.externalVisitId(),
            visit.externalTicket(),
            visit.errorCode(),
            visit.errorMessage()
        );

        return new StartSessionResponse(sessionId.toString(), null, "COMPLETED", outputMessage, visitInfo);
    }
}
