package com.clientjourney.app.service;

import com.clientjourney.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RouteValidationServiceTest {
    private final RouteValidationService service = new RouteValidationService();

    @Test
    void shouldDetectMissingStartNode() {
        ScenarioGraph graph = new ScenarioGraph(UUID.randomUUID(), 1, List.of(), List.of());
        var result = service.validate(graph);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.code().equals("START_NODE_MISSING")));
    }

    @Test
    void shouldReturnValidForSimpleReachableGraph() {
        ScenarioNode start = new ScenarioNode("start", NodeType.START, "start", "start", List.of(), List.of());
        ScenarioNode question = new ScenarioNode("q1", NodeType.QUESTION, "need", "Need?", List.of(new AnswerOption("a1", "yes", "Yes", "r1")), List.of());
        ScenarioNode result = new ScenarioNode("r1", NodeType.RESULT, "result", "Done", List.of(), List.of(new ServiceRef("svc1", "S1", "Service")));
        ScenarioGraph graph = new ScenarioGraph(UUID.randomUUID(), 1, List.of(start, question, result), List.of(new ScenarioEdge("e1", "start", null, "q1")));

        var validation = service.validate(graph);
        assertTrue(validation.valid());
        assertTrue(validation.errors().isEmpty());
        assertTrue(validation.warnings().isEmpty());
    }
}
