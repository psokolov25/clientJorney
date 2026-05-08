package com.clientjourney.domain.model;

import java.util.List;
import java.util.UUID;

public record ScenarioGraph(
    UUID scenarioId,
    int version,
    List<ScenarioNode> nodes,
    List<ScenarioEdge> edges
) {
}
