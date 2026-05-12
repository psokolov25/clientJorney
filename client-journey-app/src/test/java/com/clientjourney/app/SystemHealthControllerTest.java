package com.clientjourney.app;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SystemHealthControllerTest {

    @Test
    void probes_should_return_expected_statuses() {
        SystemHealthController controller = new SystemHealthController();

        Map<String, Object> health = controller.health();
        Map<String, Object> readiness = controller.readiness();
        Map<String, Object> liveness = controller.liveness();

        assertEquals("UP", health.get("status"));
        assertEquals("READY", readiness.get("status"));
        assertEquals("ALIVE", liveness.get("status"));

        assertNotNull(health.get("timestamp"));
        assertNotNull(readiness.get("timestamp"));
        assertNotNull(liveness.get("timestamp"));
    }
}
