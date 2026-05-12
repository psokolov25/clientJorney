package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.CreateScenarioRequest;
import com.clientjourney.app.admin.dto.ScenarioImportRequest;
import com.clientjourney.app.repository.InMemoryScenarioGraphRepository;
import com.clientjourney.app.repository.InMemoryScenarioRepository;
import com.clientjourney.app.service.RouteValidationService;
import com.clientjourney.app.service.ScenarioGraphService;
import com.clientjourney.app.service.ScenarioService;
import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.ScenarioNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminScenarioTransferControllerTest {
    @Test
    void should_import_and_export_scenario() {
        ScenarioService ss = new ScenarioService(new InMemoryScenarioRepository());
        ScenarioGraphService gs = new ScenarioGraphService(ss, new InMemoryScenarioGraphRepository(), new RouteValidationService());
        AdminScenarioTransferController c = new AdminScenarioTransferController(ss, gs, new com.fasterxml.jackson.databind.ObjectMapper());

        ScenarioGraph graph = new ScenarioGraph(
                null,
                0,
                List.of(new ScenarioNode("n1", NodeType.QUESTION, "q", "", List.of(), List.of())),
                List.of()
        );

        Scenario created = c.importScenario(new ScenarioImportRequest(new CreateScenarioRequest("code-1", "name", "desc"), graph));
        var exported = c.exportScenario(created.id());
        assertEquals("code-1", exported.scenario().code());
        assertEquals(created.id(), exported.graph().scenarioId());

        var dryRun = c.dryRunImport(new ScenarioImportRequest(new CreateScenarioRequest("code-2", "n", "d"), null));
        assertTrue(dryRun.ok());
        assertEquals(0, dryRun.issuesCount());
        assertTrue(dryRun.issues().isEmpty());

        var duplicate = c.validateImport(new ScenarioImportRequest(new CreateScenarioRequest("code-1", "n", "d"), null));
        assertFalse(duplicate.valid());

        var invalid = c.validateImport(new ScenarioImportRequest(new CreateScenarioRequest("", "n", "d"), null));
        assertFalse(invalid.valid());
    }

    @Test
    void validateImportShouldReturnGraphIssues() {
        ScenarioService ss = new ScenarioService(new InMemoryScenarioRepository());
        ScenarioGraphService gs = new ScenarioGraphService(ss, new InMemoryScenarioGraphRepository(), new RouteValidationService());
        AdminScenarioTransferController c = new AdminScenarioTransferController(ss, gs, new com.fasterxml.jackson.databind.ObjectMapper());

        var invalidGraph = c.validateImport(new ScenarioImportRequest(
                new CreateScenarioRequest("code-x", "name", "desc"),
                new ScenarioGraph(null, 0, List.of(), List.of())
        ));

        assertFalse(invalidGraph.valid());
        assertTrue(invalidGraph.issues().contains("graph.nodes must not be empty when graph is provided"));
    }


    @Test
    void validateImportShouldReturnVersionCompatibilityIssues() {
        ScenarioService ss = new ScenarioService(new InMemoryScenarioRepository());
        ScenarioGraphService gs = new ScenarioGraphService(ss, new InMemoryScenarioGraphRepository(), new RouteValidationService());
        AdminScenarioTransferController c = new AdminScenarioTransferController(ss, gs, new com.fasterxml.jackson.databind.ObjectMapper());

        ScenarioGraph incompatible = new ScenarioGraph(
                java.util.UUID.randomUUID(),
                3,
                List.of(new ScenarioNode("n1", NodeType.QUESTION, "q", "", List.of(), List.of())),
                List.of()
        );

        var result = c.validateImport(new ScenarioImportRequest(
                new CreateScenarioRequest("code-y", "name", "desc"),
                incompatible
        ));

        assertFalse(result.valid());
        assertTrue(result.issues().contains("graph.scenarioId must be empty for import payload"));
        assertTrue(result.issues().contains("graph.version is incompatible; expected 0 or 1 for initial import"));
    }



    @Test
    void dryRunShouldReturnIssueListForInvalidPayload() {
        ScenarioService ss = new ScenarioService(new InMemoryScenarioRepository());
        ScenarioGraphService gs = new ScenarioGraphService(ss, new InMemoryScenarioGraphRepository(), new RouteValidationService());
        AdminScenarioTransferController c = new AdminScenarioTransferController(ss, gs, new com.fasterxml.jackson.databind.ObjectMapper());

        var dryRun = c.dryRunImport(new ScenarioImportRequest(
                new CreateScenarioRequest("", "name", "desc"),
                new ScenarioGraph(java.util.UUID.randomUUID(), 3, List.of(), List.of())
        ));

        assertFalse(dryRun.ok());
        assertTrue(dryRun.issuesCount() >= 3);
        assertTrue(dryRun.issues().contains("scenario.code is required"));
        assertTrue(dryRun.issues().contains("graph.scenarioId must be empty for import payload"));
    }

}
