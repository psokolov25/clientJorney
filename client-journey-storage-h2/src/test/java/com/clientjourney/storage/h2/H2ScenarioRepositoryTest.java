package com.clientjourney.storage.h2;

import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioStatus;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class H2ScenarioRepositoryTest {

    @Test
    void shouldSaveFindAndDeleteScenario() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:scenario_repo;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        new H2SchemaInitializer(dataSource);

        H2ScenarioRepository repository = new H2ScenarioRepository(dataSource);

        Scenario scenario = new Scenario(
            UUID.randomUUID(),
            "welcome-flow",
            "Welcome Flow",
            "initial",
            ScenarioStatus.DRAFT,
            1,
            Instant.now(),
            Instant.now()
        );

        repository.save(scenario);

        assertTrue(repository.findById(scenario.id()).isPresent());
        assertTrue(repository.findByCode("welcome-flow").isPresent());
        assertEquals(1, repository.findAll().size());

        repository.deleteById(scenario.id());
        assertTrue(repository.findById(scenario.id()).isEmpty());
    }
}
