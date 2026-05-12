package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.ScenarioExportDto;
import com.clientjourney.app.admin.dto.ScenarioImportRequest;
import com.clientjourney.app.service.ScenarioGraphService;
import com.clientjourney.app.service.ScenarioService;
import com.clientjourney.domain.model.Scenario;
import io.micronaut.http.HttpStatus;
import java.util.List;
import com.clientjourney.app.admin.dto.ScenarioImportValidationResult;
import com.clientjourney.app.admin.dto.ScenarioImportDryRunResult;
import io.micronaut.http.annotation.*;

import java.util.UUID;

@Controller("/api/admin/scenarios")
public class AdminScenarioTransferController {
    private final ScenarioService scenarioService;
    private final ScenarioGraphService graphService;

    public AdminScenarioTransferController(ScenarioService scenarioService, ScenarioGraphService graphService) {
        this.scenarioService = scenarioService;
        this.graphService = graphService;
    }

    @Get("/{id}/export")
    public ScenarioExportDto exportScenario(UUID id) {
        return new ScenarioExportDto(scenarioService.findById(id), graphService.getGraph(id));
    }

    @Post("/import")
    @Status(HttpStatus.CREATED)
    public Scenario importScenario(@Body ScenarioImportRequest request) {
        Scenario created = scenarioService.create(request.scenario());
        if (request.graph() != null) {
            graphService.saveGraph(created.id(), request.graph());
        }
        return created;
    }

    @Get("/export/all")
    public List<ScenarioExportDto> exportAll() {
        return scenarioService.findAll().stream()
            .map(s -> new ScenarioExportDto(s, graphService.getGraph(s.id())))
            .toList();
    }

    @Post("/import/validate")
    public ScenarioImportValidationResult validateImport(@Body ScenarioImportRequest request) {
        if (request == null || request.scenario() == null) {
            return new ScenarioImportValidationResult(false, List.of("scenario is required"));
        }
        if (request.scenario().code() == null || request.scenario().code().isBlank()) {
            return new ScenarioImportValidationResult(false, List.of("scenario.code is required"));
        }
        return new ScenarioImportValidationResult(true, List.of());
    }

    @Post("/import/dry-run")
    public ScenarioImportDryRunResult dryRunImport(@Body ScenarioImportRequest request) {
        ScenarioImportValidationResult validation = validateImport(request);
        if (!validation.valid()) {
            return new ScenarioImportDryRunResult(false, String.join(";", validation.issues()), null);
        }
        return new ScenarioImportDryRunResult(true, "Import payload is valid", request.scenario().code());
    }
}
