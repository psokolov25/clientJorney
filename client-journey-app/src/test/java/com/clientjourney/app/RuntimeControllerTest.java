package com.clientjourney.app;

import com.clientjourney.app.dto.StartSessionRequest;
import com.clientjourney.app.dto.StartSessionResponse;
import com.clientjourney.core.ScenarioEngine;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeControllerTest {

    @Test
    void startSessionShouldReturnActiveResponseWithFallbackUser() {
        RuntimeController controller = new RuntimeController(new ScenarioEngine());

        StartSessionResponse response = controller.startSession(
                "medical-registration",
                new StartSessionRequest("REST", "", Map.of("source", "test"))
        );

        assertEquals("medical-registration", response.scenarioCode());
        assertEquals("ACTIVE", response.status());
        assertNotNull(response.sessionId());
        assertEquals("INFO", response.message().type());
        assertTrue(response.message().text().contains("anonymous"));
    }
}
