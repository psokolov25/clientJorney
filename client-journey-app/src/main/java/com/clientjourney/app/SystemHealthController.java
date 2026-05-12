package com.clientjourney.app;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.time.Instant;
import java.util.Map;

@Controller("/api/system")
public class SystemHealthController {

    @Get("/health")
    public Map<String, Object> health() {
        return status("UP", "health");
    }

    @Get("/readiness")
    public Map<String, Object> readiness() {
        return status("READY", "readiness");
    }

    @Get("/liveness")
    public Map<String, Object> liveness() {
        return status("ALIVE", "liveness");
    }

    private Map<String, Object> status(String status, String probe) {
        return Map.of(
            "status", status,
            "probe", probe,
            "timestamp", Instant.now().toString()
        );
    }
}
