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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminScenarioTransferControllerTest {
    @Test
    void should_import_and_export_scenario() {
        ScenarioService ss = new ScenarioService(new InMemoryScenarioRepository());
        ScenarioGraphService gs = new ScenarioGraphService(ss, new InMemoryScenarioGraphRepository(), new RouteValidationService());
        AdminScenarioTransferController c = new AdminScenarioTransferController(ss, gs);

        Scenario created = c.importScenario(new ScenarioImportRequest(new CreateScenarioRequest("code-1", "name", "desc"), new ScenarioGraph(null, 0, List.of(), List.of())));
        var exported = c.exportScenario(created.id());
        assertEquals("code-1", exported.scenario().code());
        assertEquals(created.id(), exported.graph().scenarioId());

        var dryRun = c.dryRunImport(new ScenarioImportRequest(new CreateScenarioRequest("code-2", "n", "d"), null));
        assertEquals(true, dryRun.ok());

        var invalid = c.validateImport(new ScenarioImportRequest(new CreateScenarioRequest("", "n", "d"), null));
        assertEquals(false, invalid.valid());
    }
}
