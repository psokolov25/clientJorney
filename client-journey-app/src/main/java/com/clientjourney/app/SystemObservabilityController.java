package com.clientjourney.app;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Controller("/api/system")
public class SystemObservabilityController {
    private final AtomicLong metricsRequests = new AtomicLong();

    @Get("/metrics")
    public Map<String, Object> metrics() {
        long count = metricsRequests.incrementAndGet();
        return Map.of(
            "requests.metrics", count,
            "jvm.uptime.ms", ManagementFactory.getRuntimeMXBean().getUptime(),
            "timestamp", Instant.now().toString()
        );
    }

    @Get("/trace")
    public Map<String, Object> trace(@Header(value = "X-Correlation-Id", defaultValue = "") String correlationId) {
        String id = correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId;
        return Map.of("correlationId", id, "status", "TRACED", "header", HttpHeaders.CONTENT_TYPE);
    }
}
