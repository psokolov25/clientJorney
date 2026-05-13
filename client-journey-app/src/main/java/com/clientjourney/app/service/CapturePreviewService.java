package com.clientjourney.app.service;

import com.clientjourney.app.admin.dto.CapturePreviewRequest;
import com.clientjourney.app.admin.dto.CapturePreviewResponse;
import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.ScenarioNode;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
public class CapturePreviewService {
    private final CaptureNodeProcessingService captureNodeProcessingService;

    public CapturePreviewService(CaptureNodeProcessingService captureNodeProcessingService) {
        this.captureNodeProcessingService = captureNodeProcessingService;
    }

    public CapturePreviewResponse preview(ScenarioGraph graph, CapturePreviewRequest request) {
        ScenarioNode node = graph.nodes().stream()
            .filter(n -> n.id().equals(request.nodeId()))
            .findFirst()
            .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Node not found: " + request.nodeId()));
        if (node.type() != NodeType.API_CAPTURE && node.type() != NodeType.GROOVY_CAPTURE) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Node must be API_CAPTURE or GROOVY_CAPTURE");
        }
        Map<String, Object> result = captureNodeProcessingService.process(
            node,
            request.answerValue(),
            request.metadata() == null ? Map.of() : request.metadata()
        );
        return new CapturePreviewResponse(node.id(), node.type().name(), result);
    }
}
