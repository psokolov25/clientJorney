package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

import java.util.Map;

@Introspected
public record StartSessionRequest(String channel, String externalUserId, Map<String, Object> metadata) {
}
