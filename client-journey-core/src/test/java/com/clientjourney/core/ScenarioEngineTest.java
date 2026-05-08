package com.clientjourney.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioEngineTest {

    @Test
    void startSessionShouldReturnActiveStatusAndSessionId() {
        ScenarioEngine engine = new ScenarioEngine();

        SessionStartResult result = engine.startSession("medical-registration", "client-123");

        assertEquals("medical-registration", result.scenarioCode());
        assertEquals("client-123", result.externalUserId());
        assertEquals("ACTIVE", result.status());
        assertNotNull(result.sessionId());
        assertFalse(result.sessionId().isBlank());
    }
}
