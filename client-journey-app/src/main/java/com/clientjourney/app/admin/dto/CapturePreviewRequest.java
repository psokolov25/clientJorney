package com.clientjourney.app.admin.dto;

import io.micronaut.core.annotation.Introspected;

import java.util.Map;

@Introspected
public record CapturePreviewRequest(
    String nodeId,
    String answerValue,
    Map<String, Object> metadata
) {}
