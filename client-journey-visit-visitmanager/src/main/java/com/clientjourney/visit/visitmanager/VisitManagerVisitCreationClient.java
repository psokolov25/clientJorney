package com.clientjourney.visit.visitmanager;

import com.clientjourney.visit.spi.VisitCreationClient;
import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationResult;
import com.clientjourney.visit.spi.VisitCreationStatus;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Objects;

@Requires(property = "client-journey.visit.client", value = "visitmanager")
@Singleton
public class VisitManagerVisitCreationClient implements VisitCreationClient {
    private final boolean enabled;
    private final String entrypointCode;

    public VisitManagerVisitCreationClient(
        @Value("${client-journey.visit.visitmanager.enabled:false}") boolean enabled,
        @Value("${client-journey.visit.visitmanager.entrypoint-code:ENTRYPOINT_WITH_PARAMETERS}") String entrypointCode
    ) {
        this.enabled = enabled;
        this.entrypointCode = entrypointCode;
    }

    @Override
    public VisitCreationResult createVisit(VisitCreationRequest request) {
        if (!enabled) {
            return new VisitCreationResult(
                VisitCreationStatus.SKIPPED,
                "VISIT_MANAGER",
                null,
                null,
                "DISABLED",
                "VisitManager client is disabled"
            );
        }

        Map<String, String> params = request.parameters() == null ? Map.of() : request.parameters();
        String serviceCount = params.getOrDefault("selectedServicesCount", "0");
        String channel = Objects.requireNonNullElse(request.channel(), "unknown");
        String externalVisitId = "vm-" + request.sessionId();
        String ticket = entrypointCode + "-" + request.scenarioCode() + "-" + serviceCount + "-" + channel;

        return new VisitCreationResult(
            VisitCreationStatus.SUCCESS,
            "VISIT_MANAGER",
            externalVisitId,
            ticket,
            null,
            "Visit created via " + entrypointCode
        );
    }
}
