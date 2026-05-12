package com.clientjourney.app;

import io.micronaut.context.annotation.Value;
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
    private final AtomicLong traceRequests = new AtomicLong();
    private final AtomicLong traceErrors = new AtomicLong();
    private final AtomicLong traceLatencyNanosTotal = new AtomicLong();

    private final String visitProviderStatus;
    private final String storageStatus;
    private final long storageLatencyMs;
    private final long storageLatencyWarnThresholdMs;

    public SystemObservabilityController(
        @Value("${clientjourney.observability.dependency.visit-provider-status:UP}") String visitProviderStatus,
        @Value("${clientjourney.observability.dependency.storage-status:UP}") String storageStatus,
        @Value("${clientjourney.observability.dependency.storage-latency-ms:0}") long storageLatencyMs,
        @Value("${clientjourney.observability.dependency.storage-latency-warn-threshold-ms:200}") long storageLatencyWarnThresholdMs
    ) {
        this.visitProviderStatus = visitProviderStatus;
        this.storageStatus = storageStatus;
        this.storageLatencyMs = storageLatencyMs;
        this.storageLatencyWarnThresholdMs = storageLatencyWarnThresholdMs;
    }

    SystemObservabilityController() {
        this("UP", "UP", 0, 200);
    }

    @Get("/metrics")
    public Map<String, Object> metrics() {
        long count = metricsRequests.incrementAndGet();
        long traces = traceRequests.get();
        long errors = traceErrors.get();
        long totalLatencyNanos = traceLatencyNanosTotal.get();
        long avgLatencyMs = traces == 0 ? 0 : (totalLatencyNanos / traces) / 1_000_000;

        return Map.of(
            "requests.metrics", count,
            "requests.trace", traces,
            "errors.trace", errors,
            "error.rate.trace", traces == 0 ? 0.0d : ((double) errors / traces),
            "latency.trace.avg.ms", avgLatencyMs,
            "jvm.uptime.ms", ManagementFactory.getRuntimeMXBean().getUptime(),
            "timestamp", Instant.now().toString()
        );
    }

    @Get("/trace")
    public Map<String, Object> trace(@Header(value = "X-Correlation-Id", defaultValue = "") String correlationId) {
        long started = System.nanoTime();
        try {
            String id = correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId;
            traceRequests.incrementAndGet();
            return Map.of("correlationId", id, "status", "TRACED", "header", HttpHeaders.CONTENT_TYPE);
        } catch (RuntimeException ex) {
            traceErrors.incrementAndGet();
            throw ex;
        } finally {
            traceLatencyNanosTotal.addAndGet(System.nanoTime() - started);
        }
    }


    @Get("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "timestamp", Instant.now().toString());
    }

    @Get("/readiness")
    public Map<String, Object> readiness() {
        return Map.of("status", "READY", "timestamp", Instant.now().toString());
    }

    @Get("/liveness")
    public Map<String, Object> liveness() {
        return Map.of("status", "ALIVE", "timestamp", Instant.now().toString());
    }

    @Get("/dependencies")
    public Map<String, Object> dependencies() {
        return Map.of(
            "visitProvider", Map.of("status", visitProviderStatus),
            "storage", Map.of(
                "status", storageStatus,
                "latencyMs", storageLatencyMs,
                "thresholdMs", storageLatencyWarnThresholdMs,
                "degradedByLatency", storageLatencyMs > storageLatencyWarnThresholdMs
            )
        );
    }

    @Get("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> metrics = metrics();
        Map<String, Object> dependencies = dependencies();

        boolean degraded = !"UP".equalsIgnoreCase(visitProviderStatus)
            || !"UP".equalsIgnoreCase(storageStatus)
            || storageLatencyMs > storageLatencyWarnThresholdMs;

        java.util.List<String> alerts = new java.util.ArrayList<>();
        if (!"UP".equalsIgnoreCase(visitProviderStatus)) alerts.add("VISIT_PROVIDER_DOWN");
        if (!"UP".equalsIgnoreCase(storageStatus)) alerts.add("STORAGE_DOWN");
        if (storageLatencyMs > storageLatencyWarnThresholdMs) alerts.add("STORAGE_LATENCY_HIGH");
        if (alerts.isEmpty()) alerts.add("ALL_SYSTEMS_NOMINAL");

        return Map.of(
            "health", health().get("status"),
            "readiness", readiness().get("status"),
            "liveness", liveness().get("status"),
            "degraded", degraded,
            "alerts", alerts,
            "dependencies", dependencies,
            "metrics", metrics,
            "timestamp", Instant.now().toString()
        );
    }
}
