package com.clientjourney.app.service;

import com.clientjourney.app.dto.AnswerRequest;
import com.clientjourney.app.dto.OutputMessage;
import com.clientjourney.app.dto.StartSessionResponse;
import com.clientjourney.core.ScenarioEngine;
import com.clientjourney.core.ScenarioStepResult;
import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.ScenarioNode;
import com.clientjourney.storage.spi.ScenarioGraphRepository;
import com.clientjourney.storage.spi.ScenarioRepository;
import jakarta.inject.Singleton;

import java.util.UUID;

@Singleton
public class RuntimeAnswerService {
    private final ConversationSessionService conversationSessionService;
    private final ScenarioEngine scenarioEngine;
    private final ScenarioRepository scenarioRepository;
    private final ScenarioGraphRepository scenarioGraphRepository;
    private final CaptureNodeProcessingService captureNodeProcessingService;


    public RuntimeAnswerService(ConversationSessionService conversationSessionService) {
        this(conversationSessionService,
            new ScenarioEngine(),
            new com.clientjourney.app.repository.InMemoryScenarioRepository(),
            new com.clientjourney.app.repository.InMemoryScenarioGraphRepository(),
            new CaptureNodeProcessingService(null));
    }

    public RuntimeAnswerService(ConversationSessionService conversationSessionService,
                                ScenarioEngine scenarioEngine,
                                ScenarioRepository scenarioRepository,
                                ScenarioGraphRepository scenarioGraphRepository,
                                CaptureNodeProcessingService captureNodeProcessingService) {
        this.conversationSessionService = conversationSessionService;
        this.scenarioEngine = scenarioEngine;
        this.scenarioRepository = scenarioRepository;
        this.scenarioGraphRepository = scenarioGraphRepository;
        this.captureNodeProcessingService = captureNodeProcessingService;
    }

    public StartSessionResponse processAnswer(UUID sessionId, AnswerRequest request) {
        ConversationSessionService.SessionState state = conversationSessionService.findSession(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        String currentNodeId = String.valueOf(state.metadata().get("currentNodeId"));
        try {
            Scenario scenario = scenarioRepository.findByCode(state.scenarioCode())
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + state.scenarioCode()));
            ScenarioGraph graph = scenarioGraphRepository.findByScenarioIdAndVersion(scenario.id(), scenario.version())
                .orElseThrow(() -> new IllegalArgumentException("Scenario graph not found for scenario: " + state.scenarioCode()));
            ScenarioNode currentNode = graph.nodes().stream()
                .filter(n -> n.id().equals(currentNodeId))
                .findFirst()
                .orElse(null);
            if (currentNode != null && (currentNode.type() == NodeType.API_CAPTURE || currentNode.type() == NodeType.GROOVY_CAPTURE)) {
                var captured = captureNodeProcessingService.process(currentNode, request.answerValue(), state.metadata());
                Object lastOutput = captured.get("capture.lastOutput");
                if (lastOutput instanceof java.util.Map<?, ?> outputMap) {
                    if (outputMap.containsKey("__nextInput")) {
                        captured.put("capture.nextInput", outputMap.get("__nextInput"));
                    } else if (outputMap.containsKey("nextInput")) {
                        captured.put("capture.nextInput", outputMap.get("nextInput"));
                    }
                }
                conversationSessionService.mergeMetadata(sessionId, captured);
            }

            ScenarioStepResult next = scenarioEngine.nextStep(graph, currentNodeId, request.answerCode());
            conversationSessionService.updateCurrentNodeId(sessionId, next.nodeId());

            String status = next.completed() ? "COMPLETED" : "ACTIVE";
            if (next.completed() && next.services() != null && !next.services().isEmpty()) {
                var allowedServices = next.services().stream()
                    .map(s -> new com.clientjourney.app.dto.SelectedServiceDto(s.serviceId(), s.serviceCode(), s.serviceName()))
                    .toList();
                return StartSessionResponse.withoutVisitCreation(sessionId.toString(), state.scenarioCode(), "ACTIVE", OutputMessage.serviceSelection(next.text(), 1, 3, allowedServices));
            }
            return StartSessionResponse.withoutVisitCreation(sessionId.toString(), state.scenarioCode(), status, OutputMessage.info(next.text()));
        } catch (IllegalArgumentException ex) {
            String text = "Answer accepted: " + (request.answerCode() == null ? "unknown" : request.answerCode());
            return StartSessionResponse.withoutVisitCreation(sessionId.toString(), state.scenarioCode(), "ACTIVE", OutputMessage.info(text));
        }
    }
}
