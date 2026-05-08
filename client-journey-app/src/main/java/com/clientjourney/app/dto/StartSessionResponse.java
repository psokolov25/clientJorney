package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record StartSessionResponse(String sessionId, String scenarioCode, String status, OutputMessage message) {
}
