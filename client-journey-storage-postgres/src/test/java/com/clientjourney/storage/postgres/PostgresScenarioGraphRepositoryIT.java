package com.clientjourney.storage.postgres;

import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.ScenarioEdge;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.ScenarioNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgresScenarioGraphRepositoryIT {

    @Test
    void shouldSaveAndReadGraphInIntegrationProfile() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:pg_graph;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        new PostgresSchemaInitializer(dataSource);

        PostgresScenarioGraphRepository repository = new PostgresScenarioGraphRepository(dataSource, new ObjectMapper());
        UUID scenarioId = UUID.randomUUID();
        ScenarioGraph graph = new ScenarioGraph(scenarioId, 1,
            List.of(new ScenarioNode("n1", NodeType.START, "code", "text", List.of(), List.of())),
            List.of(new ScenarioEdge("e1", "n1", null, "n1")));

        repository.save(graph);
        ScenarioGraph loaded = repository.findByScenarioIdAndVersion(scenarioId, 1).orElseThrow();
        assertEquals(1, loaded.nodes().size());
    }
}
