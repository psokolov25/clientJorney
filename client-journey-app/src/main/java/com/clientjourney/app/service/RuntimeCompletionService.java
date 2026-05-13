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
    private final VisitCreationSettingsService visitCreationSettingsService;

    public RuntimeCompletionService(
        ServiceSelectionProcessor serviceSelectionProcessor,
        ConversationSessionService conversationSessionService,
        VisitCreationClient visitCreationClient,
        VisitCreationSettingsService visitCreationSettingsService
    ) {
        this.serviceSelectionProcessor = serviceSelectionProcessor;
        this.conversationSessionService = conversationSessionService;
        this.visitCreationClient = visitCreationClient;
        this.visitCreationSettingsService = visitCreationSettingsService;
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
        String branchId = parameters.getOrDefault("branchId", "");
        String scenarioIdRaw = parameters.get("scenarioId");
        if (scenarioIdRaw != null && !scenarioIdRaw.isBlank()) {
            try {
                UUID scenarioId = UUID.fromString(scenarioIdRaw);
                var visitSettings = visitCreationSettingsService.get(scenarioId);
                String resolvedBaseUrl = visitCreationSettingsService.resolveBaseUrl(scenarioId, branchId);
                parameters.put("visitProvider", visitSettings.provider());
                parameters.put("visitMode", visitSettings.mode());
                if (resolvedBaseUrl != null && !resolvedBaseUrl.isBlank()) {
                    parameters.put("visitBaseUrl", resolvedBaseUrl);
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed scenarioId in metadata and continue with baseline flow.
            }
        }

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
