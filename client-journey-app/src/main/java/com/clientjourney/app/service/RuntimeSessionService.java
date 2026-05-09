package com.clientjourney.app.service;

import com.clientjourney.app.dto.OutputMessage;
import com.clientjourney.app.dto.SelectedServiceDto;
import com.clientjourney.app.dto.StartSessionResponse;
import com.clientjourney.core.ScenarioEngine;
import com.clientjourney.core.SessionStartResult;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.UUID;

@Singleton
public class RuntimeSessionService {
    private final ScenarioEngine scenarioEngine;
    private final ConversationSessionService conversationSessionService;

    public RuntimeSessionService(ScenarioEngine scenarioEngine, ConversationSessionService conversationSessionService) {
        this.scenarioEngine = scenarioEngine;
        this.conversationSessionService = conversationSessionService;
    }

    public StartSessionResponse startSession(String scenarioCode, String externalUserId) {
        SessionStartResult result = scenarioEngine.startSession(scenarioCode, externalUserId);
        UUID sessionId = UUID.fromString(result.sessionId());

        List<SelectedServiceDto> allowedServices = defaultAllowedServices();
        conversationSessionService.registerSession(sessionId, allowedServices, 1, 3);

        return StartSessionResponse.withoutVisitCreation(
            result.sessionId(),
            result.scenarioCode(),
            "ACTIVE",
            OutputMessage.serviceSelection("Выберите одну или несколько услуг", 1, 3, allowedServices)
        );
    }

    private List<SelectedServiceDto> defaultAllowedServices() {
        return List.of(
            new SelectedServiceDto("svc-1", "THERAPIST", "Therapist"),
            new SelectedServiceDto("svc-2", "CARDIOLOGY", "Cardiology"),
            new SelectedServiceDto("svc-3", "ANALYSIS", "Analysis")
        );
    }
}
