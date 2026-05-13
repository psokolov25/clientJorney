package com.clientjourney.app.service;

import com.clientjourney.app.admin.dto.GraphValidationIssue;
import com.clientjourney.app.admin.dto.GraphValidationResult;
import com.clientjourney.domain.model.*;
import jakarta.inject.Singleton;

import java.util.*;

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
        }

        for (ScenarioNode node : nodes) {
            if (node.type() == NodeType.QUESTION && (node.answers() == null || node.answers().isEmpty())) {
                errors.add(new GraphValidationIssue("QUESTION_WITHOUT_ANSWERS", node.id(), "Question node must contain at least one answer"));
            }
            if (node.type() == NodeType.RESULT && (node.services() == null || node.services().isEmpty())) {
                errors.add(new GraphValidationIssue("RESULT_WITHOUT_SERVICES", node.id(), "Result node must contain at least one service"));
            }
            if ((node.type() == NodeType.API_CAPTURE || node.type() == NodeType.GROOVY_CAPTURE)
                && (node.code() == null || node.code().isBlank())) {
                errors.add(new GraphValidationIssue("CAPTURE_NODE_WITHOUT_CODE", node.id(), "Capture node must contain endpoint or script in code field"));
            }
            if (node.answers() != null) {
                Set<String> answerIds = new HashSet<>();
                for (AnswerOption answer : node.answers()) {
                    if (answer.id() == null || answer.id().isBlank()) {
                        errors.add(new GraphValidationIssue("ANSWER_ID_MISSING", node.id(), "Answer id must be present"));
                    } else if (!answerIds.add(answer.id())) {
                        errors.add(new GraphValidationIssue("DUPLICATE_ANSWER_ID", node.id(), "Duplicate answer id inside node"));
                    }
                }
            }
            if (node.type() == NodeType.QUESTION && node.answers() != null) {
                for (AnswerOption answer : node.answers()) {
                    if (answer.nextNodeId() == null || answer.nextNodeId().isBlank()) {
                        errors.add(new GraphValidationIssue("ANSWER_NEXT_NODE_MISSING", node.id(), "Question answer must point to next node"));
                    } else if (!nodeIds.contains(answer.nextNodeId())) {
                        errors.add(new GraphValidationIssue("ANSWER_NEXT_NODE_NOT_FOUND", node.id(), "Question answer points to missing node"));
                    }
                }
            }
        }

        Set<String> edgeIds = new HashSet<>();
        for (ScenarioEdge edge : edges) {
            if (!edgeIds.add(edge.id())) {
                errors.add(new GraphValidationIssue("DUPLICATE_EDGE_ID", edge.id(), "Duplicate edge id detected"));
            }
            if (!nodeIds.contains(edge.sourceNodeId())) {
                errors.add(new GraphValidationIssue("EDGE_SOURCE_NOT_FOUND", edge.sourceNodeId(), "Edge source node not found"));
            }
            if (!nodeIds.contains(edge.targetNodeId())) {
                errors.add(new GraphValidationIssue("EDGE_TARGET_NOT_FOUND", edge.targetNodeId(), "Edge target node not found"));
            }
        }

        Map<String, Set<String>> adjacency = buildAdjacency(nodes, edges);

        Optional<ScenarioNode> startNode = nodes.stream().filter(n -> n.type() == NodeType.START).findFirst();
        if (startNode.isPresent()) {
            if (adjacency.getOrDefault(startNode.get().id(), Set.of()).isEmpty()) {
                errors.add(new GraphValidationIssue("START_WITHOUT_OUTGOING", startNode.get().id(), "START node must have outgoing transition"));
            }

            Set<String> reachable = bfs(startNode.get().id(), adjacency);
            for (ScenarioNode node : nodes) {
                if (!reachable.contains(node.id())) {
                    warnings.add(new GraphValidationIssue("UNREACHABLE_NODE", node.id(), "Node is unreachable from START"));
                }
            }
        }

        for (ScenarioNode node : nodes) {
            if (node.type() == NodeType.QUESTION || node.type() == NodeType.INFO) {
                if (adjacency.getOrDefault(node.id(), Set.of()).isEmpty()) {
                    errors.add(new GraphValidationIssue("DEAD_END_NODE", node.id(), "Question/Info node must have outgoing transition"));
                }
            }
        }

        return new GraphValidationResult(errors.isEmpty(), errors, warnings);
    }

    private Map<String, Set<String>> buildAdjacency(List<ScenarioNode> nodes, List<ScenarioEdge> edges) {
        Map<String, Set<String>> adjacency = new HashMap<>();
        for (ScenarioEdge edge : edges) {
            adjacency.computeIfAbsent(edge.sourceNodeId(), k -> new HashSet<>()).add(edge.targetNodeId());
        }
        for (ScenarioNode node : nodes) {
            if (node.answers() != null) {
                node.answers().stream().filter(a -> a.nextNodeId() != null && !a.nextNodeId().isBlank()).forEach(a ->
                    adjacency.computeIfAbsent(node.id(), k -> new HashSet<>()).add(a.nextNodeId())
                );
            }
        }
        return adjacency;
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
