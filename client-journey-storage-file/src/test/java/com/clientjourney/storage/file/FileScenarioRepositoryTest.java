package com.clientjourney.storage.file;

import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileScenarioRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void saveCreatesBackupOnOverwrite() {
        Path filePath = tempDir.resolve("scenarios.json");
        FileScenarioRepository repository = new FileScenarioRepository(new ObjectMapper().findAndRegisterModules(), filePath);
        Scenario scenario = scenario("A1");

        repository.save(scenario);
        repository.save(scenario("A2"));

        assertTrue(Files.exists(filePath));
        assertTrue(Files.exists(tempDir.resolve("scenarios.json.bak")));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void corruptedJsonIsMovedAndExceptionThrown() throws Exception {
        Path filePath = tempDir.resolve("scenarios.json");
        Files.writeString(filePath, "{broken-json");

        CorruptedStorageException ex = assertThrows(
            CorruptedStorageException.class,
            () -> new FileScenarioRepository(new ObjectMapper().findAndRegisterModules(), filePath)
        );

        assertTrue(ex.getMessage().contains("Storage is corrupted"));
        assertFalse(Files.exists(filePath));
        assertTrue(Files.list(tempDir).anyMatch(path -> path.getFileName().toString().startsWith("scenarios.json.corrupted.")));
    }

    private Scenario scenario(String code) {
        return new Scenario(UUID.randomUUID(), code, "name", "desc", ScenarioStatus.DRAFT, 1, Instant.now(), Instant.now());
    }
}
