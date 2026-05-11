package com.clientjourney.visit.custom;

import com.clientjourney.visit.spi.VisitCreationClient;
import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationResult;
import com.clientjourney.visit.spi.VisitCreationStatus;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Requires(property = "client-journey.visit.client", value = "custom-rest")
@Singleton
public class CustomRestVisitCreationClient implements VisitCreationClient {
    private final boolean enabled;
    private final String baseUrl;

    public CustomRestVisitCreationClient(
        @Value("${client-journey.visit.custom-rest.enabled:false}") boolean enabled,
        @Value("${client-journey.visit.custom-rest.base-url:http://localhost:8081}") String baseUrl
    ) {
        this.enabled = enabled;
        this.baseUrl = baseUrl;
    }

    @Override
    public VisitCreationResult createVisit(VisitCreationRequest request) {
        if (!enabled) {
            return new VisitCreationResult(
                VisitCreationStatus.SKIPPED,
                "CUSTOM_REST",
                null,
                null,
                "DISABLED",
                "Custom REST visit client is disabled"
            );
        }

        return new VisitCreationResult(
            VisitCreationStatus.FAILED,
            "CUSTOM_REST",
            null,
            null,
            "NOT_IMPLEMENTED",
            "HTTP integration is not implemented yet. Configure endpoint: " + baseUrl
        );
    }
}
