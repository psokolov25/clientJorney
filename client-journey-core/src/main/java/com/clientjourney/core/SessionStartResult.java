package com.clientjourney.core;

public record SessionStartResult(String sessionId, String scenarioCode, String externalUserId, String status) {
}
