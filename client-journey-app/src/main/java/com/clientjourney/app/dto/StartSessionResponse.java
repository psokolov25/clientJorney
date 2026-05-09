package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record StartSessionResponse(
    String sessionId,
    String scenarioCode,
    String status,
    OutputMessage message,
    VisitCreationInfo visitCreation
) {
    public static StartSessionResponse withoutVisitCreation(
        String sessionId,
        String scenarioCode,
        String status,
        OutputMessage message
    ) {
        return new StartSessionResponse(sessionId, scenarioCode, status, message, null);
    }
}
