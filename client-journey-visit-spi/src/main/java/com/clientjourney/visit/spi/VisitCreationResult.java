package com.clientjourney.visit.spi;

public record VisitCreationResult(
        VisitCreationStatus status,
        String clientType,
        String externalVisitId,
        String externalTicket,
        String errorCode,
        String errorMessage
) {
}
