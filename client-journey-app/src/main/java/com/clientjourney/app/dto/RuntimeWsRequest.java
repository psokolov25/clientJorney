package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

import java.util.List;
import java.util.Map;

@Introspected
public record RuntimeWsRequest(
    String action,
    String scenarioCode,
    String sessionId,
    String answerCode,
    String answerValue,
    java.util.List<SelectedServiceDto> selectedServices,
    String externalUserId,
    String channel,
    Map<String, Object> metadata
) {
}
