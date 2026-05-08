package com.clientjourney.app.admin;

import com.clientjourney.app.repository.InMemoryScenarioGraphRepository;
import com.clientjourney.app.repository.InMemoryScenarioRepository;
import com.clientjourney.app.service.RouteValidationService;
import com.clientjourney.app.service.ScenarioGraphService;
import com.clientjourney.app.service.ScenarioService;
import com.clientjourney.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminScenarioGraphControllerTest {

    @Test
    void shouldSaveGetAndValidateGraph() {
        var scenarioService = new ScenarioService(new InMemoryScenarioRepository());
        var controller = new AdminScenarioGraphController(new ScenarioGraphService(
            scenarioService,
            new InMemoryScenarioGraphRepository(),
            new RouteValidationService()
        ));
        var scenario = new AdminScenarioController(scenarioService)
            .create(new com.clientjourney.app.admin.dto.CreateScenarioRequest("code", "name", "desc"));

        ScenarioGraph graph = new ScenarioGraph(scenario.id(), scenario.version(), List.of(
            new ScenarioNode("start", NodeType.START, "start", "start", List.of(), List.of()),
            new ScenarioNode("result", NodeType.RESULT, "result", "done", List.of(), List.of(new ServiceRef("svc", "S", "service")))
        ), List.of(new ScenarioEdge("e1", "start", null, "result")));

        ScenarioGraph saved = controller.putGraph(scenario.id(), graph);
        assertEquals(scenario.id(), saved.scenarioId());

        ScenarioGraph loaded = controller.getGraph(scenario.id());
        assertEquals(2, loaded.nodes().size());

        var validation = controller.validate(scenario.id());
        assertTrue(validation.valid());
    }
}
