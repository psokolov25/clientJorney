package com.clientjourney.core;

import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.ScenarioEdge;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.ScenarioNode;
import jakarta.inject.Singleton;

import java.util.*;

@Singleton
public class ScenarioEngine {
    public SessionStartResult startSession(String scenarioCode, String externalUserId) {
        return new SessionStartResult(UUID.randomUUID().toString(), scenarioCode, externalUserId, "ACTIVE");
    }

    public ScenarioStepResult firstStep(ScenarioGraph graph) {
        ScenarioNode start = graph.nodes().stream().filter(n -> n.type() == NodeType.START).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("START node not found"));
        String next = nextFromNode(graph, start.id(), null);
        ScenarioNode node = byId(graph, next);
        return new ScenarioStepResult(node.id(), node.type().name(), node.text(), node.type() == NodeType.RESULT, node.services() == null ? List.of() : node.services());
    }

    public ScenarioStepResult nextStep(ScenarioGraph graph, String currentNodeId, String answerCode) {
        ScenarioNode current = byId(graph, currentNodeId);
        String next = nextFromNode(graph, current.id(), answerCode);
        ScenarioNode node = byId(graph, next);
        return new ScenarioStepResult(node.id(), node.type().name(), node.text(), node.type() == NodeType.RESULT, node.services() == null ? List.of() : node.services());
    }

    private ScenarioNode byId(ScenarioGraph graph, String nodeId) {
        return graph.nodes().stream().filter(n -> Objects.equals(n.id(), nodeId)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));
    }

    private String nextFromNode(ScenarioGraph graph, String sourceNodeId, String answerCode) {
        return graph.edges().stream()
            .filter(e -> Objects.equals(e.sourceNodeId(), sourceNodeId))
            .filter(e -> answerCode == null || Objects.equals(e.sourceAnswerId(), answerCode) || e.sourceAnswerId() == null)
            .map(ScenarioEdge::targetNodeId)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Next node is not defined for source=" + sourceNodeId));
    }
}
