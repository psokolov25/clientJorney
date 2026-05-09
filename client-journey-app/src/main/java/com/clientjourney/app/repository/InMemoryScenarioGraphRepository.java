package com.clientjourney.app.repository;

import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.storage.spi.ScenarioGraphRepository;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Requires(property = "client-journey.storage.type", value = "in-memory")
@Singleton
public class InMemoryScenarioGraphRepository implements ScenarioGraphRepository {
    private final Map<String, ScenarioGraph> graphs = new ConcurrentHashMap<>();

    @Override
    public ScenarioGraph save(ScenarioGraph graph) {
        graphs.put(key(graph.scenarioId(), graph.version()), graph);
        return graph;
    }

    @Override
    public Optional<ScenarioGraph> findByScenarioIdAndVersion(UUID scenarioId, int version) {
        return Optional.ofNullable(graphs.get(key(scenarioId, version)));
    }

    private String key(UUID scenarioId, int version) {
        return scenarioId + ":" + version;
    }
}
