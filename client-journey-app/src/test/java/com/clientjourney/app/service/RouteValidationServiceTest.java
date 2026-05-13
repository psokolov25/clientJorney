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
    void shouldDetectStartWithoutOutgoingTransition() {
        ScenarioNode start = new ScenarioNode("start", NodeType.START, "start", "Start", List.of(), List.of());
        ScenarioGraph graph = new ScenarioGraph(UUID.randomUUID(), 1, List.of(start), List.of());

        var validation = service.validate(graph);
        assertTrue(validation.errors().stream().anyMatch(e -> e.code().equals("START_WITHOUT_OUTGOING")));
    }

    @Test
    void shouldDetectQuestionAnswerWithoutNextNode() {
        ScenarioNode start = new ScenarioNode("start", NodeType.START, "start", "Start", List.of(), List.of());
        ScenarioNode question = new ScenarioNode("q1", NodeType.QUESTION, "need", "Need?", List.of(new AnswerOption("a1", "yes", "Yes", null)), List.of());
        ScenarioGraph graph = new ScenarioGraph(UUID.randomUUID(), 1, List.of(start, question), List.of(new ScenarioEdge("e1", "start", null, "q1")));

        var validation = service.validate(graph);
        assertTrue(validation.errors().stream().anyMatch(e -> e.code().equals("ANSWER_NEXT_NODE_MISSING")));
    }

    @Test
    void shouldDetectDuplicateAnswerIdsInsideQuestionNode() {
        ScenarioNode start = new ScenarioNode("start", NodeType.START, "start", "start", List.of(), List.of());
        ScenarioNode result = new ScenarioNode("r1", NodeType.RESULT, "result", "Done", List.of(), List.of(new ServiceRef("svc1", "S1", "Service")));
        ScenarioNode question = new ScenarioNode("q1", NodeType.QUESTION, "need", "Need?", List.of(
            new AnswerOption("a1", "yes", "Yes", "r1"),
            new AnswerOption("a1", "no", "No", "r1")
        ), List.of());
        ScenarioGraph graph = new ScenarioGraph(UUID.randomUUID(), 1, List.of(start, question, result), List.of(new ScenarioEdge("e1", "start", null, "q1")));

        var validation = service.validate(graph);
        assertTrue(validation.errors().stream().anyMatch(e -> e.code().equals("DUPLICATE_ANSWER_ID")));
    }

    @Test
    void shouldDetectAnswerNextNodeNotFound() {
        ScenarioNode start = new ScenarioNode("start", NodeType.START, "start", "start", List.of(), List.of());
        ScenarioNode question = new ScenarioNode("q1", NodeType.QUESTION, "need", "Need?", List.of(new AnswerOption("a1", "yes", "Yes", "missing")), List.of());
        ScenarioGraph graph = new ScenarioGraph(UUID.randomUUID(), 1, List.of(start, question), List.of(new ScenarioEdge("e1", "start", null, "q1")));

        var validation = service.validate(graph);
        assertTrue(validation.errors().stream().anyMatch(e -> e.code().equals("ANSWER_NEXT_NODE_NOT_FOUND")));
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

    @Test
    void shouldDetectCaptureNodeWithoutCode() {
        ScenarioNode start = new ScenarioNode("start", NodeType.START, "start", "start", List.of(), List.of());
        ScenarioNode apiCapture = new ScenarioNode("api1", NodeType.API_CAPTURE, "", "Call API", List.of(new AnswerOption("a1", "ok", "OK", "r1")), List.of());
        ScenarioNode result = new ScenarioNode("r1", NodeType.RESULT, "result", "Done", List.of(), List.of(new ServiceRef("svc1", "S1", "Service")));
        ScenarioGraph graph = new ScenarioGraph(UUID.randomUUID(), 1, List.of(start, apiCapture, result), List.of(new ScenarioEdge("e1", "start", null, "api1")));

        var validation = service.validate(graph);
        assertTrue(validation.errors().stream().anyMatch(e -> e.code().equals("CAPTURE_NODE_WITHOUT_CODE")));
    }
}
