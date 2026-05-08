package com.clientjourney.storage.spi;

import com.clientjourney.domain.model.ScenarioGraph;

import java.util.Optional;
import java.util.UUID;

public interface ScenarioGraphRepository {
    ScenarioGraph save(ScenarioGraph graph);

    Optional<ScenarioGraph> findByScenarioIdAndVersion(UUID scenarioId, int version);
}
