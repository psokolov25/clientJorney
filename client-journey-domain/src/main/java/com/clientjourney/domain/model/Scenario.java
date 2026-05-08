package com.clientjourney.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Scenario(
    UUID id,
    String code,
    String name,
    String description,
    ScenarioStatus status,
    int version,
    Instant createdAt,
    Instant updatedAt
) {
}
