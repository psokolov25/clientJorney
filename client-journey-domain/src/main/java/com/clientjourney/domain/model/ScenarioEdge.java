package com.clientjourney.domain.model;

public record ScenarioEdge(
    String id,
    String sourceNodeId,
    String sourceAnswerId,
    String targetNodeId
) {
}
