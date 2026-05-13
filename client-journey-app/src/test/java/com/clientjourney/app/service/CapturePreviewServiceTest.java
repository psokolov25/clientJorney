package com.clientjourney.app.service;

import com.clientjourney.app.admin.dto.CapturePreviewRequest;
import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.ScenarioNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
