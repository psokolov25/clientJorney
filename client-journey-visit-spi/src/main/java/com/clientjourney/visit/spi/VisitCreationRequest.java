package com.clientjourney.visit.spi;

import java.util.Map;
import java.util.UUID;

public record VisitCreationRequest(
        UUID sessionId,
        String scenarioCode,
        int scenarioVersion,
        String channel,
        String externalUserId,
        Map<String, String> parameters
) {
}
