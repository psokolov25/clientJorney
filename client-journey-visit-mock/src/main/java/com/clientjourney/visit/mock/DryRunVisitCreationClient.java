package com.clientjourney.visit.mock;

import com.clientjourney.visit.spi.VisitCreationClient;
import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationResult;
import com.clientjourney.visit.spi.VisitCreationStatus;
import jakarta.inject.Singleton;

@Singleton
public class DryRunVisitCreationClient implements VisitCreationClient {
    @Override
    public VisitCreationResult createVisit(VisitCreationRequest request) {
        return new VisitCreationResult(
            VisitCreationStatus.DRY_RUN,
            "DRY_RUN",
            "dry-run-" + request.sessionId(),
            "DRY-001",
            null,
            "Dry-run completed"
        );
    }
}
