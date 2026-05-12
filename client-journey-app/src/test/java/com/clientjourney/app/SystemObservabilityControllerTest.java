package com.clientjourney.app;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SystemObservabilityControllerTest {
    @Test
    void metrics_trace_health_and_dashboard_should_work() {
        SystemObservabilityController c = new SystemObservabilityController();
        Map<String, Object> m1 = c.metrics();
        Map<String, Object> m2 = c.metrics();
        assertEquals(1L, m1.get("requests.metrics"));
        assertEquals(2L, m2.get("requests.metrics"));

        Map<String, Object> traced = c.trace("abc-123");
        assertEquals("abc-123", traced.get("correlationId"));
        Map<String, Object> tracedGenerated = c.trace("");
        assertNotNull(tracedGenerated.get("correlationId"));

        Map<String, Object> health = c.health();
        assertEquals("UP", health.get("status"));

        Map<String, Object> readiness = c.readiness();
        assertEquals("READY", readiness.get("status"));

        Map<String, Object> liveness = c.liveness();
        assertEquals("ALIVE", liveness.get("status"));

        Map<String, Object> m3 = c.metrics();
        assertEquals(2L, m3.get("requests.trace"));
        assertEquals(0L, m3.get("errors.trace"));
        assertNotNull(m3.get("error.rate.trace"));
        assertNotNull(m3.get("latency.trace.avg.ms"));

        Map<String, Object> dashboard = c.dashboard();
        assertEquals("UP", dashboard.get("health"));
        assertEquals("READY", dashboard.get("readiness"));
        assertEquals("ALIVE", dashboard.get("liveness"));
        assertNotNull(dashboard.get("timestamp"));
        assertInstanceOf(Map.class, dashboard.get("metrics"));
        assertInstanceOf(Map.class, dashboard.get("dependencies"));
        assertInstanceOf(java.util.List.class, dashboard.get("alerts"));
        assertEquals(false, dashboard.get("degraded"));

        Map<String, Object> deps = c.dependencies();
        assertInstanceOf(Map.class, deps.get("visitProvider"));
        assertInstanceOf(Map.class, deps.get("storage"));

        SystemObservabilityController degradedController = new SystemObservabilityController("DOWN", "UP", 500, 200);
        Map<String, Object> degraded = degradedController.dashboard();
        assertEquals(true, degraded.get("degraded"));
        assertInstanceOf(Map.class, degraded.get("dependencies"));
    }
}
