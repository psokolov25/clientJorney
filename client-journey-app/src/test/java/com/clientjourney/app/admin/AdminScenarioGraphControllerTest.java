package com.clientjourney.app.admin;

import com.clientjourney.app.repository.InMemoryScenarioGraphRepository;
import com.clientjourney.app.repository.InMemoryScenarioRepository;
import com.clientjourney.app.service.RouteValidationService;
import com.clientjourney.app.service.CaptureNodeProcessingService;
import com.clientjourney.app.service.CapturePreviewService;
import com.clientjourney.app.service.ScenarioGraphService;
import com.clientjourney.app.service.ScenarioService;
import com.clientjourney.domain.model.*;
import org.junit.jupiter.api.Test;
import io.micronaut.http.exceptions.HttpStatusException;

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
        ), new CapturePreviewService(new CaptureNodeProcessingService(null)));
        var scenario = new AdminScenarioController(scenarioService)
            .create(new com.clientjourney.app.admin.dto.CreateScenarioRequest("code", "name", "desc"));

        ScenarioGraph graph = new ScenarioGraph(scenario.id(), scenario.version(), List.of(
            new ScenarioNode("start", NodeType.START, "start", "start", List.of(), List.of()),
            new ScenarioNode("result", NodeType.RESULT, "result", "done", List.of(), List.of(new ServiceRef("svc", "S", "service")))
        ), List.of(new ScenarioEdge("e1", "start", null, "result")));

        ScenarioGraph saved = controller.putGraph(
            scenario.id(),
            graph,
            "\"" + scenario.version() + "\"",
            String.valueOf(scenario.version())
        ).body();
        assertEquals(scenario.id(), saved.scenarioId());

        var response = controller.getGraph(scenario.id());
        ScenarioGraph loaded = response.body();
        assertEquals(2, loaded.nodes().size());
        assertNotNull(response.header("ETag"));

        var validation = controller.validate(scenario.id());
        assertTrue(validation.valid());
    }

    @Test
    void shouldFailOnVersionConflictWhenIfMatchDoesNotMatchScenarioVersion() {
        var scenarioService = new ScenarioService(new InMemoryScenarioRepository());
        var controller = new AdminScenarioGraphController(new ScenarioGraphService(
            scenarioService,
            new InMemoryScenarioGraphRepository(),
            new RouteValidationService()
        ), new CapturePreviewService(new CaptureNodeProcessingService(null)));
        var scenario = new AdminScenarioController(scenarioService)
            .create(new com.clientjourney.app.admin.dto.CreateScenarioRequest("code2", "name2", "desc2"));

        ScenarioGraph graph = new ScenarioGraph(scenario.id(), scenario.version(), List.of(
            new ScenarioNode("start", NodeType.START, "start", "start", List.of(), List.of()),
            new ScenarioNode("result", NodeType.RESULT, "result", "done", List.of(), List.of(new ServiceRef("svc", "S", "service")))
        ), List.of(new ScenarioEdge("e1", "start", null, "result")));

        HttpStatusException ex = assertThrows(HttpStatusException.class, () ->
            controller.putGraph(scenario.id(), graph, "\"999\"", "999")
        );
        assertEquals(409, ex.getStatus().getCode());
    }

    @Test
    void shouldRequirePreconditionHeadersWhenSavingGraph() {
        var scenarioService = new ScenarioService(new InMemoryScenarioRepository());
        var controller = new AdminScenarioGraphController(new ScenarioGraphService(
            scenarioService,
            new InMemoryScenarioGraphRepository(),
            new RouteValidationService()
        ), new CapturePreviewService(new CaptureNodeProcessingService(null)));
        var scenario = new AdminScenarioController(scenarioService)
            .create(new com.clientjourney.app.admin.dto.CreateScenarioRequest("code3", "name3", "desc3"));

        ScenarioGraph graph = new ScenarioGraph(scenario.id(), scenario.version(), List.of(
            new ScenarioNode("start", NodeType.START, "start", "start", List.of(), List.of()),
            new ScenarioNode("result", NodeType.RESULT, "result", "done", List.of(), List.of(new ServiceRef("svc", "S", "service")))
        ), List.of(new ScenarioEdge("e1", "start", null, "result")));

        HttpStatusException ex = assertThrows(HttpStatusException.class, () ->
            controller.putGraph(scenario.id(), graph, "", "")
        );
        assertEquals(428, ex.getStatus().getCode());
    }

    @Test
    void shouldAcceptIfMatchHeaderWithMultipleEtagValues() {
        var scenarioService = new ScenarioService(new InMemoryScenarioRepository());
        var controller = new AdminScenarioGraphController(new ScenarioGraphService(
            scenarioService,
            new InMemoryScenarioGraphRepository(),
            new RouteValidationService()
        ), new CapturePreviewService(new CaptureNodeProcessingService(null)));
        var scenario = new AdminScenarioController(scenarioService)
            .create(new com.clientjourney.app.admin.dto.CreateScenarioRequest("code4", "name4", "desc4"));

        ScenarioGraph graph = new ScenarioGraph(scenario.id(), scenario.version(), List.of(
            new ScenarioNode("start", NodeType.START, "start", "start", List.of(), List.of()),
            new ScenarioNode("result", NodeType.RESULT, "result", "done", List.of(), List.of(new ServiceRef("svc", "S", "service")))
        ), List.of(new ScenarioEdge("e1", "start", null, "result")));

        ScenarioGraph saved = controller.putGraph(
            scenario.id(),
            graph,
            "\"999\", W/\"" + scenario.version() + "\"",
            ""
        ).body();
        assertEquals(scenario.id(), saved.scenarioId());
    }

    @Test
    void shouldAcceptWildcardIfMatchPrecondition() {
        var scenarioService = new ScenarioService(new InMemoryScenarioRepository());
        var controller = new AdminScenarioGraphController(new ScenarioGraphService(
            scenarioService,
            new InMemoryScenarioGraphRepository(),
            new RouteValidationService()
        ), new CapturePreviewService(new CaptureNodeProcessingService(null)));
        var scenario = new AdminScenarioController(scenarioService)
            .create(new com.clientjourney.app.admin.dto.CreateScenarioRequest("code5", "name5", "desc5"));

        ScenarioGraph graph = new ScenarioGraph(scenario.id(), scenario.version(), List.of(
            new ScenarioNode("start", NodeType.START, "start", "start", List.of(), List.of()),
            new ScenarioNode("result", NodeType.RESULT, "result", "done", List.of(), List.of(new ServiceRef("svc", "S", "service")))
        ), List.of(new ScenarioEdge("e1", "start", null, "result")));

        ScenarioGraph saved = controller.putGraph(
            scenario.id(),
            graph,
            "*",
            ""
        ).body();
        assertEquals(scenario.id(), saved.scenarioId());
    }
}
