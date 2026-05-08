package com.clientjourney.app.service;

import com.clientjourney.app.admin.dto.CreateScenarioRequest;
import com.clientjourney.app.admin.dto.UpdateScenarioRequest;
import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioStatus;
import com.clientjourney.storage.spi.ScenarioRepository;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.List;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import java.util.UUID;

@Singleton
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;

    public ScenarioService(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    public List<Scenario> findAll() {
        return scenarioRepository.findAll();
    }

    public Scenario create(CreateScenarioRequest request) {
        Scenario scenario = new Scenario(
            UUID.randomUUID(),
            request.code(),
            request.name(),
            request.description(),
            ScenarioStatus.DRAFT,
            1,
            Instant.now(),
            Instant.now()
        );
        return scenarioRepository.save(scenario);
    }

    public Scenario findById(UUID id) {
        return scenarioRepository.findById(id).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Scenario not found: " + id));
    }

    public Scenario update(UUID id, UpdateScenarioRequest request) {
        Scenario existing = findById(id);
        Scenario updated = new Scenario(
            existing.id(),
            existing.code(),
            request.name() == null ? existing.name() : request.name(),
            request.description() == null ? existing.description() : request.description(),
            existing.status(),
            existing.version(),
            existing.createdAt(),
            Instant.now()
        );
        return scenarioRepository.save(updated);
    }

    public void delete(UUID id) {
        scenarioRepository.deleteById(id);
    }

    public Scenario publish(UUID id) {
        Scenario existing = findById(id);
        Scenario published = new Scenario(
            existing.id(),
            existing.code(),
            existing.name(),
            existing.description(),
            ScenarioStatus.PUBLISHED,
            existing.version(),
            existing.createdAt(),
            Instant.now()
        );
        return scenarioRepository.save(published);
    }

    public Scenario archive(UUID id) {
        Scenario existing = findById(id);
        Scenario archived = new Scenario(
            existing.id(),
            existing.code(),
            existing.name(),
            existing.description(),
            ScenarioStatus.ARCHIVED,
            existing.version(),
            existing.createdAt(),
            Instant.now()
        );
        return scenarioRepository.save(archived);
    }

    public Scenario cloneVersion(UUID id) {
        Scenario existing = findById(id);
        Scenario cloned = new Scenario(
            existing.id(),
            existing.code(),
            existing.name(),
            existing.description(),
            ScenarioStatus.DRAFT,
            existing.version() + 1,
            existing.createdAt(),
            Instant.now()
        );
        return scenarioRepository.save(cloned);
    }
}
