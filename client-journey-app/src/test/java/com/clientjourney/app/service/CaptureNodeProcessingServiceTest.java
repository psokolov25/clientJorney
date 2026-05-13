package com.clientjourney.app.service;

import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.ScenarioNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CaptureNodeProcessingServiceTest {

    @Test
    void shouldWrapGroovyCaptureResultAndExposeNextInput() {
        CaptureNodeProcessingService service = new CaptureNodeProcessingService(null);
        ScenarioNode node = new ScenarioNode(
            "groovy-1",
            NodeType.GROOVY_CAPTURE,
            "return [__nextInput: 'crm-42', segment: 'VIP']",
            "Groovy capture",
            List.of(),
            List.of()
        );

        Map<String, Object> result = service.process(node, "photo-json", Map.of("capture.lastOutput", Map.of("foo", "bar")));

        assertEquals("groovy-1", result.get("capture.lastNodeId"));
        assertEquals("GROOVY_CAPTURE", result.get("capture.lastType"));
        assertTrue(result.containsKey("capture.lastTraceId"));
        assertTrue(result.containsKey("capture.node.groovy-1"));

        Object payload = result.get("capture.lastOutput");
        assertInstanceOf(Map.class, payload);
        Map<?, ?> payloadMap = (Map<?, ?>) payload;
        assertEquals("crm-42", payloadMap.get("__nextInput"));
        assertEquals("crm-42", payloadMap.get("nextInput"));
        assertEquals("VIP", payloadMap.get("segment"));
    }

    @Test
    void shouldRejectUnsupportedApiMethod() {
        CaptureNodeProcessingService service = new CaptureNodeProcessingService(null);
        ScenarioNode node = new ScenarioNode(
            "api-1",
            NodeType.API_CAPTURE,
            "{\"url\":\"https://example.org/capture\",\"method\":\"PUT\"}",
            "API capture",
            List.of(),
            List.of()
        );

        Map<String, Object> result = service.process(node, "value", Map.of());

        Map<?, ?> payload = (Map<?, ?>) result.get("capture.lastOutput");
        assertEquals("Unsupported method: PUT. Allowed: GET, POST", payload.get("capture.api.error"));
    }

    @Test
    void shouldRejectNonHttpUrlAndHandleNullMetadata() {
        CaptureNodeProcessingService service = new CaptureNodeProcessingService(null);
        ScenarioNode node = new ScenarioNode(
            "api-2",
            NodeType.API_CAPTURE,
            "ftp://example.org/capture",
            "API capture",
            List.of(),
            List.of()
        );

        Map<String, Object> result = service.process(node, "value", null);

        Map<?, ?> payload = (Map<?, ?>) result.get("capture.lastOutput");
        assertEquals("Unsupported url: only absolute http(s) URLs are allowed", payload.get("capture.api.error"));
    }

    @Test
    void shouldReportInvalidApiCaptureConfigJson() {
        CaptureNodeProcessingService service = new CaptureNodeProcessingService(null);
        ScenarioNode node = new ScenarioNode(
            "api-3",
            NodeType.API_CAPTURE,
            "{\"url\":\"https://example.org/capture\",",
            "API capture",
            List.of(),
            List.of()
        );

        Map<String, Object> result = service.process(node, "value", Map.of());

        Map<?, ?> payload = (Map<?, ?>) result.get("capture.lastOutput");
        assertTrue(String.valueOf(payload.get("capture.api.error")).startsWith("Invalid API_CAPTURE config JSON:"));
    }

    @Test
    void shouldReportGroovyRuntimeErrorInCaptureOutput() {
        CaptureNodeProcessingService service = new CaptureNodeProcessingService(null);
        ScenarioNode node = new ScenarioNode(
            "groovy-err",
            NodeType.GROOVY_CAPTURE,
            "throw new RuntimeException('boom')",
            "Groovy capture",
            List.of(),
            List.of()
        );

        Map<String, Object> result = service.process(node, "value", Map.of());

        Map<?, ?> payload = (Map<?, ?>) result.get("capture.lastOutput");
        assertTrue(String.valueOf(payload.get("capture.groovy.error")).contains("boom"));
    }
}
