package com.clientjourney.app.service;

import com.clientjourney.app.admin.dto.CapturePreviewRequest;
import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.ScenarioNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CapturePreviewServiceTest {

    @Test
    void shouldPreviewGroovyCaptureNode() {
        CapturePreviewService service = new CapturePreviewService(new CaptureNodeProcessingService(null));
        ScenarioGraph graph = new ScenarioGraph(
            UUID.randomUUID(),
            1,
            List.of(new ScenarioNode("g1", NodeType.GROOVY_CAPTURE, "return [__nextInput: 'abc']", "Groovy", List.of(), List.of())),
            List.of()
        );

        var response = service.preview(graph, new CapturePreviewRequest("g1", "payload", Map.of()));

        assertEquals("g1", response.nodeId());
        assertEquals("GROOVY_CAPTURE", response.nodeType());
        assertEquals("abc", ((Map<?, ?>) response.result().get("capture.lastOutput")).get("__nextInput"));
    }

    @Test
    void shouldReturnDeterministicErrorForInvalidApiCaptureConfig() {
        CapturePreviewService service = new CapturePreviewService(new CaptureNodeProcessingService(null));
        ScenarioGraph graph = new ScenarioGraph(
            UUID.randomUUID(),
            1,
            List.of(new ScenarioNode("api1", NodeType.API_CAPTURE, "{\"url\":\"https://example.org\",", "Api", List.of(), List.of())),
            List.of()
        );

        var response = service.preview(graph, new CapturePreviewRequest("api1", "payload", Map.of()));
        Map<?, ?> output = (Map<?, ?>) response.result().get("capture.lastOutput");

        assertTrue(String.valueOf(output.get("capture.api.error")).startsWith("Invalid API_CAPTURE config JSON:"));
    }

    @Test
    void shouldReturnGroovyRuntimeErrorInPreview() {
        CapturePreviewService service = new CapturePreviewService(new CaptureNodeProcessingService(null));
        ScenarioGraph graph = new ScenarioGraph(
            UUID.randomUUID(),
            1,
            List.of(new ScenarioNode("g-err", NodeType.GROOVY_CAPTURE, "throw new RuntimeException('preview-boom')", "Groovy", List.of(), List.of())),
            List.of()
        );

        var response = service.preview(graph, new CapturePreviewRequest("g-err", "payload", Map.of()));
        Map<?, ?> output = (Map<?, ?>) response.result().get("capture.lastOutput");

        assertTrue(String.valueOf(output.get("capture.groovy.error")).contains("preview-boom"));
    }

    @Test
    void shouldSupportApiToGroovyToResultPreviewFlow() {
        CapturePreviewService service = new CapturePreviewService(new CaptureNodeProcessingService(null));
        ScenarioGraph graph = new ScenarioGraph(
            UUID.randomUUID(),
            1,
            List.of(
                new ScenarioNode("api-step", NodeType.API_CAPTURE, "https://example.org/mock", "Api", List.of(), List.of()),
                new ScenarioNode("groovy-step", NodeType.GROOVY_CAPTURE, "return [__nextInput: (answerValue ?: 'na') + '-processed']", "Groovy", List.of(), List.of()),
                new ScenarioNode("result-step", NodeType.RESULT, "result", "Done", List.of(), List.of())
            ),
            List.of(
                new com.clientjourney.domain.model.ScenarioEdge("e1", "api-step", null, "groovy-step"),
                new com.clientjourney.domain.model.ScenarioEdge("e2", "groovy-step", null, "result-step")
            )
        );

        var apiResponse = service.preview(graph, new CapturePreviewRequest("api-step", "raw", Map.of()));
        assertEquals("API_CAPTURE", apiResponse.nodeType());

        var groovyResponse = service.preview(graph, new CapturePreviewRequest("groovy-step", "raw", Map.of()));
        Map<?, ?> output = (Map<?, ?>) groovyResponse.result().get("capture.lastOutput");
        assertEquals("raw-processed", output.get("__nextInput"));
    }
}
