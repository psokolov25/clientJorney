package com.clientjourney.app.service;

import com.clientjourney.app.dto.OutputMessage;
import com.clientjourney.app.dto.SelectedServiceDto;
import com.clientjourney.app.dto.StartSessionResponse;
import com.clientjourney.core.ScenarioEngine;
import com.clientjourney.core.ScenarioStepResult;
import com.clientjourney.core.SessionStartResult;
import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.storage.spi.ScenarioGraphRepository;
import com.clientjourney.storage.spi.ScenarioRepository;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class RuntimeSessionService {
    private final ScenarioEngine scenarioEngine;
    private final ConversationSessionService conversationSessionService;
    private final ScenarioRepository scenarioRepository;
    private final ScenarioGraphRepository scenarioGraphRepository;


    public RuntimeSessionService(ScenarioEngine scenarioEngine, ConversationSessionService conversationSessionService) {
        this(scenarioEngine, conversationSessionService,
            new com.clientjourney.app.repository.InMemoryScenarioRepository(),
            new com.clientjourney.app.repository.InMemoryScenarioGraphRepository());
    }

    public RuntimeSessionService(ScenarioEngine scenarioEngine,
                                 ConversationSessionService conversationSessionService,
                                 ScenarioRepository scenarioRepository,
                                 ScenarioGraphRepository scenarioGraphRepository) {
        this.scenarioEngine = scenarioEngine;
        this.conversationSessionService = conversationSessionService;
        this.scenarioRepository = scenarioRepository;
        this.scenarioGraphRepository = scenarioGraphRepository;
    }

    public StartSessionResponse startSession(String scenarioCode, String channel, String externalUserId, Map<String, Object> metadata) {
        SessionStartResult result = scenarioEngine.startSession(scenarioCode, externalUserId);
        UUID sessionId = UUID.fromString(result.sessionId());

        ScenarioStepResult firstStep;
        try {
            Scenario scenario = scenarioRepository.findByCode(scenarioCode)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found by code: " + scenarioCode));
            ScenarioGraph graph = scenarioGraphRepository.findByScenarioIdAndVersion(scenario.id(), scenario.version())
                .orElseThrow(() -> new IllegalArgumentException("Scenario graph not found for scenario: " + scenarioCode));
            firstStep = scenarioEngine.firstStep(graph);
        } catch (IllegalArgumentException ex) {
            firstStep = new ScenarioStepResult("service-selection", "RESULT", "Выберите одну или несколько услуг", true, List.of());
        }

        List<SelectedServiceDto> allowedServices = mapServices(firstStep);
        conversationSessionService.registerSession(sessionId, scenarioCode, channel, externalUserId, metadata == null ? Map.of() : metadata, allowedServices, 1, 3);
        conversationSessionService.updateCurrentNodeId(sessionId, firstStep.nodeId());

        return StartSessionResponse.withoutVisitCreation(
            result.sessionId(),
            result.scenarioCode(),
            "ACTIVE",
            OutputMessage.serviceSelection(firstStep.text(), 1, 3, allowedServices)
        );
    }

    private List<SelectedServiceDto> mapServices(ScenarioStepResult step) {
        if (step.services() == null || step.services().isEmpty()) {
            return List.of(
                new SelectedServiceDto("svc-1", "THERAPIST", "Therapist"),
                new SelectedServiceDto("svc-2", "CARDIOLOGY", "Cardiology"),
                new SelectedServiceDto("svc-3", "ANALYSIS", "Analysis")
            );
        }
        return step.services().stream()
            .map(s -> new SelectedServiceDto(s.serviceId(), s.serviceCode(), s.serviceName()))
            .toList();
    }
}
