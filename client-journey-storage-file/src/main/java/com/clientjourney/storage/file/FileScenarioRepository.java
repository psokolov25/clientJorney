package com.clientjourney.storage.file;

import com.clientjourney.domain.model.Scenario;
import com.clientjourney.storage.spi.ScenarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Requires(property = "client-journey.storage.type", value = "file")
@Singleton
public class FileScenarioRepository implements ScenarioRepository {
    private final ObjectMapper objectMapper;
    private final Path filePath;
    private final Map<UUID, Scenario> scenarios;

    public FileScenarioRepository(ObjectMapper objectMapper) {
        this(objectMapper, Path.of("data/scenarios.json"));
    }

    FileScenarioRepository(ObjectMapper objectMapper, Path filePath) {
        this.objectMapper = objectMapper;
        this.filePath = filePath;
        this.scenarios = new ConcurrentHashMap<>(loadScenarios());
    }

    @Override
    public synchronized Scenario save(Scenario scenario) {
        scenarios.put(scenario.id(), scenario);
        persist();
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
    public synchronized void deleteById(UUID id) {
        scenarios.remove(id);
        persist();
    }

    private Map<UUID, Scenario> loadScenarios() {
        if (Files.notExists(filePath)) {
            return Map.of();
        }

        try {
            List<Scenario> stored = objectMapper.readValue(filePath.toFile(), new TypeReference<>() {});
            Map<UUID, Scenario> loaded = new ConcurrentHashMap<>();
            stored.forEach(s -> loaded.put(s.id(), s));
            return loaded;
        } catch (IOException e) {
            Path corruptedPath = filePath.resolveSibling(filePath.getFileName() + ".corrupted." + Instant.now().toEpochMilli());
            try {
                Files.move(filePath, corruptedPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveEx) {
                throw new CorruptedStorageException("Storage is corrupted and cannot be moved: " + filePath, moveEx);
            }
            throw new CorruptedStorageException("Storage is corrupted and was moved to: " + corruptedPath, e);
        }
    }

    private void persist() {
        try {
            Files.createDirectories(filePath.getParent());
            Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".bak");
            if (Files.exists(filePath)) {
                Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }

            Path tempPath = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempPath.toFile(), scenarios.values());
            Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new FileStorageException("Failed to persist scenarios into " + filePath, e);
        }
    }
}
