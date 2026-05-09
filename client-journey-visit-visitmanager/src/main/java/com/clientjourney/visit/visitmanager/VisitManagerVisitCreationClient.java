package com.clientjourney.visit.visitmanager;

import com.clientjourney.visit.spi.VisitCreationClient;
import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationResult;
import com.clientjourney.visit.spi.VisitCreationStatus;
import jakarta.inject.Singleton;

@Singleton
public class VisitManagerVisitCreationClient implements VisitCreationClient {
    @Override
    public VisitCreationResult createVisit(VisitCreationRequest request) {
        return new VisitCreationResult(
            VisitCreationStatus.SKIPPED,
            "VISIT_MANAGER",
            null,
            null,
            "NOT_IMPLEMENTED",
            "VisitManager client skeleton is not implemented yet"
        );
    }
}
