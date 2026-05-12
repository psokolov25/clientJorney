package com.clientjourney.app;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SystemObservabilityControllerTest {
    @Test
    void metrics_and_trace_should_work() {
        SystemObservabilityController c = new SystemObservabilityController();
        Map<String,Object> m1 = c.metrics();
        Map<String,Object> m2 = c.metrics();
        assertEquals(1L, m1.get("requests.metrics"));
        assertEquals(2L, m2.get("requests.metrics"));

        Map<String,Object> traced = c.trace("abc-123");
        assertEquals("abc-123", traced.get("correlationId"));
    }
}
