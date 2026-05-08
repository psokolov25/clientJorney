package com.clientjourney.core;

import jakarta.inject.Singleton;

import java.util.UUID;

@Singleton
public class ScenarioEngine {
    public SessionStartResult startSession(String scenarioCode, String externalUserId) {
        return new SessionStartResult(
                UUID.randomUUID().toString(),
                scenarioCode,
                externalUserId,
                "ACTIVE"
        );
    }
}
