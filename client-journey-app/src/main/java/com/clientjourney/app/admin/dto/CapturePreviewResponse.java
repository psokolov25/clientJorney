package com.clientjourney.app.admin.dto;

import io.micronaut.core.annotation.Introspected;

import java.util.Map;

@Introspected
public record CapturePreviewResponse(
    String nodeId,
    String nodeType,
    Map<String, Object> result
) {}
