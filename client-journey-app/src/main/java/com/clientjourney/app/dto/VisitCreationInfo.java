package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record VisitCreationInfo(
    String status,
    String clientType,
    String externalVisitId,
    String ticket,
    String errorCode,
    String errorMessage
) {
}
