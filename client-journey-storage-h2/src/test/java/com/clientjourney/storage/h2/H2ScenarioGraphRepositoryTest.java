package com.clientjourney.storage.h2;

import com.clientjourney.domain.model.AnswerOption;
import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.ScenarioEdge;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.ScenarioNode;
import com.clientjourney.domain.model.ServiceRef;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class H2ScenarioGraphRepositoryTest {

    @Test
    void shouldSaveAndReadGraph() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:graph_repo;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        new H2SchemaInitializer(dataSource);

        H2ScenarioGraphRepository repository = new H2ScenarioGraphRepository(dataSource, new ObjectMapper());

        UUID scenarioId = UUID.randomUUID();
        ScenarioGraph graph = new ScenarioGraph(
            scenarioId,
            1,
            List.of(new ScenarioNode("start", NodeType.START, "start", "Start", List.of(new AnswerOption("a1", "yes", "Yes", "result")), List.of() ),
                new ScenarioNode("result", NodeType.RESULT, "result", "Done", List.of(), List.of(new ServiceRef("1", "svc", "Service")))),
            List.of(new ScenarioEdge("e1", "start", "a1", "result"))
        );

        repository.save(graph);

        ScenarioGraph loaded = repository.findByScenarioIdAndVersion(scenarioId, 1).orElseThrow();
        assertEquals(2, loaded.nodes().size());
        assertEquals(1, loaded.edges().size());
        assertEquals("result", loaded.edges().get(0).targetNodeId());
    }
}
