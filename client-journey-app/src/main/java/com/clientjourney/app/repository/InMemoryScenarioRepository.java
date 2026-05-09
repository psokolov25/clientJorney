package com.clientjourney.app.repository;

import com.clientjourney.domain.model.Scenario;
import com.clientjourney.storage.spi.ScenarioRepository;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Requires(property = "client-journey.storage.type", value = "in-memory")
@Singleton
public class InMemoryScenarioRepository implements ScenarioRepository {
    private final Map<UUID, Scenario> scenarios = new ConcurrentHashMap<>();

    @Override
    public Scenario save(Scenario scenario) {
        scenarios.put(scenario.id(), scenario);
        return scenario;
    }

    @Override
    public Optional<Scenario> findById(UUID id) {
        return Optional.ofNullable(scenarios.get(id));
    }

    @Override
    public Optional<Scenario> findByCode(String code) {
        return scenarios.values().stream().filter(s -> s.code().equals(code)).findFirst();
    }

    @Override
    public List<Scenario> findAll() {
        return new ArrayList<>(scenarios.values());
    }

    @Override
    public void deleteById(UUID id) {
        scenarios.remove(id);
    }
}
