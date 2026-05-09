package com.clientjourney.channel.spi;

import java.util.Map;
import java.util.UUID;

public record ClientInputMessage(
    UUID sessionId,
    String scenarioCode,
    String channel,
    String externalUserId,
    String messageType,
    String answerCode,
    String answerValue,
    Map<String, Object> metadata
) {
}
