package com.clientjourney.storage.postgres;

import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioStatus;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgresScenarioRepositoryTest {
    @Test
    void shouldPersistScenarioCrud() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:pg_repo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        new PostgresSchemaInitializer(dataSource);

        PostgresScenarioRepository repository = new PostgresScenarioRepository(dataSource);
        Scenario scenario = new Scenario(UUID.randomUUID(), "code-1", "Name", "Desc", ScenarioStatus.DRAFT, 1, Instant.now(), Instant.now());

        repository.save(scenario);
        assertTrue(repository.findById(scenario.id()).isPresent());
        assertEquals(1, repository.findAll().size());

        repository.deleteById(scenario.id());
        assertTrue(repository.findById(scenario.id()).isEmpty());
    }
}
