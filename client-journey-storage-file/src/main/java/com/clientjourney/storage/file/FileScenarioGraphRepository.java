package com.clientjourney.storage.file;

import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.storage.spi.ScenarioGraphRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Requires(property = "client-journey.storage.type", value = "file")
@Singleton
public class FileScenarioGraphRepository implements ScenarioGraphRepository {
    private final ObjectMapper objectMapper;
    private final Path filePath;
    private final Map<String, ScenarioGraph> graphs;

    public FileScenarioGraphRepository(ObjectMapper objectMapper) {
        this(objectMapper, Path.of("data/scenario-graphs.json"));
    }

    FileScenarioGraphRepository(ObjectMapper objectMapper, Path filePath) {
        this.objectMapper = objectMapper;
        this.filePath = filePath;
        this.graphs = new ConcurrentHashMap<>(loadGraphs());
    }

    @Override
    public synchronized ScenarioGraph save(ScenarioGraph graph) {
        graphs.put(key(graph.scenarioId(), graph.version()), graph);
        persist();
        return graph;
    }

    @Override
    public Optional<ScenarioGraph> findByScenarioIdAndVersion(UUID scenarioId, int version) {
        return Optional.ofNullable(graphs.get(key(scenarioId, version)));
    }

    private Map<String, ScenarioGraph> loadGraphs() {
        if (Files.notExists(filePath)) {
            return Map.of();
        }

        try {
            List<ScenarioGraph> stored = objectMapper.readValue(filePath.toFile(), new TypeReference<>() {});
            Map<String, ScenarioGraph> loaded = new ConcurrentHashMap<>();
            stored.forEach(g -> loaded.put(key(g.scenarioId(), g.version()), g));
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
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempPath.toFile(), graphs.values());
            Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new FileStorageException("Failed to persist scenario graphs into " + filePath, e);
        }
    }

    private String key(UUID scenarioId, int version) {
        return scenarioId + ":" + version;
    }
}
