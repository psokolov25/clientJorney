package com.clientjourney.visit.custom;

import com.clientjourney.visit.spi.VisitCreationClient;
import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationResult;
import com.clientjourney.visit.spi.VisitCreationStatus;
import jakarta.inject.Singleton;

@Singleton
public class CustomRestVisitCreationClient implements VisitCreationClient {
    @Override
    public VisitCreationResult createVisit(VisitCreationRequest request) {
        return new VisitCreationResult(
            VisitCreationStatus.SKIPPED,
            "CUSTOM_REST",
            null,
            null,
            "NOT_IMPLEMENTED",
            "Custom REST client skeleton is not implemented yet"
        );
    }
}
