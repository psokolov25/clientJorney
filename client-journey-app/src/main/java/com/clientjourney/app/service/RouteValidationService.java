package com.clientjourney.app.service;

import com.clientjourney.app.admin.dto.GraphValidationIssue;
import com.clientjourney.app.admin.dto.GraphValidationResult;
import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.ScenarioEdge;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.ScenarioNode;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class RouteValidationService {
    public GraphValidationResult validate(ScenarioGraph graph) {
        List<GraphValidationIssue> errors = new ArrayList<>();
        List<GraphValidationIssue> warnings = new ArrayList<>();

        List<ScenarioNode> nodes = Optional.ofNullable(graph.nodes()).orElse(List.of());
        List<ScenarioEdge> edges = Optional.ofNullable(graph.edges()).orElse(List.of());

        long startCount = nodes.stream().filter(n -> n.type() == NodeType.START).count();
        if (startCount == 0) {
            errors.add(new GraphValidationIssue("START_NODE_MISSING", null, "Graph must contain exactly one START node"));
        } else if (startCount > 1) {
            errors.add(new GraphValidationIssue("MULTIPLE_START_NODES", null, "Graph contains multiple START nodes"));
        }

        Set<String> nodeIds = new HashSet<>();
        for (ScenarioNode node : nodes) {
            if (!nodeIds.add(node.id())) {
                errors.add(new GraphValidationIssue("DUPLICATE_NODE_ID", node.id(), "Duplicate node id detected"));
            }
            if (node.type() == NodeType.QUESTION && (node.answers() == null || node.answers().isEmpty())) {
                errors.add(new GraphValidationIssue("QUESTION_WITHOUT_ANSWERS", node.id(), "Question node must contain at least one answer"));
            }
            if (node.type() == NodeType.RESULT && (node.services() == null || node.services().isEmpty())) {
                errors.add(new GraphValidationIssue("RESULT_WITHOUT_SERVICES", node.id(), "Result node must contain at least one service"));
            }
        }

        for (ScenarioEdge edge : edges) {
            if (!nodeIds.contains(edge.targetNodeId())) {
                errors.add(new GraphValidationIssue("EDGE_TARGET_NOT_FOUND", edge.targetNodeId(), "Edge target node not found"));
            }
        }

        Map<String, Set<String>> adjacency = new HashMap<>();
        for (ScenarioEdge edge : edges) {
            adjacency.computeIfAbsent(edge.sourceNodeId(), k -> new HashSet<>()).add(edge.targetNodeId());
        }
        for (ScenarioNode node : nodes) {
            if (node.answers() != null) {
                node.answers().stream().filter(a -> a.nextNodeId() != null).forEach(a ->
                    adjacency.computeIfAbsent(node.id(), k -> new HashSet<>()).add(a.nextNodeId())
                );
            }
        }

        Optional<ScenarioNode> startNode = nodes.stream().filter(n -> n.type() == NodeType.START).findFirst();
        if (startNode.isPresent()) {
            Set<String> reachable = bfs(startNode.get().id(), adjacency);
            for (ScenarioNode node : nodes) {
                if (!reachable.contains(node.id())) {
                    warnings.add(new GraphValidationIssue("UNREACHABLE_NODE", node.id(), "Node is unreachable from START"));
                }
            }
        }

        return new GraphValidationResult(errors.isEmpty(), errors, warnings);
    }

    private Set<String> bfs(String root, Map<String, Set<String>> adjacency) {
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(root);
        visited.add(root);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (String next : adjacency.getOrDefault(current, Set.of())) {
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return visited;
    }
}
