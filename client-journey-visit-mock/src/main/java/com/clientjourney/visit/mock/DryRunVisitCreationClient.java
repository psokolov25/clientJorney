package com.clientjourney.visit.mock;

import com.clientjourney.visit.spi.VisitCreationClient;
import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationResult;
import com.clientjourney.visit.spi.VisitCreationStatus;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Requires(missingProperty = "client-journey.visit.client")
@Requires(property = "client-journey.visit.client", value = "dry-run", defaultValue = "dry-run")
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
