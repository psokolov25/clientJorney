package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.ScenarioExportDto;
import com.clientjourney.app.admin.dto.ScenarioImportRequest;
import com.clientjourney.app.service.ScenarioGraphService;
import com.clientjourney.app.service.ScenarioService;
import com.clientjourney.domain.model.Scenario;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import java.util.ArrayList;
import java.util.List;
import com.clientjourney.app.admin.dto.ScenarioImportValidationResult;
import com.clientjourney.app.admin.dto.ScenarioImportDryRunResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.multipart.CompletedFileUpload;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import io.micronaut.http.annotation.*;

import java.util.UUID;

@Controller("/api/admin/scenarios")
public class AdminScenarioTransferController {
    private final ScenarioService scenarioService;
    private final ScenarioGraphService graphService;
    private final ObjectMapper objectMapper;

    public AdminScenarioTransferController(ScenarioService scenarioService, ScenarioGraphService graphService, ObjectMapper objectMapper) {
        this.scenarioService = scenarioService;
        this.graphService = graphService;
        this.objectMapper = objectMapper;
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



    @Post(value = "/import/file", consumes = io.micronaut.http.MediaType.MULTIPART_FORM_DATA)
    @Status(HttpStatus.CREATED)
    public Scenario importScenarioFile(@Part("file") CompletedFileUpload file) throws IOException {
        byte[] bytes = file.getBytes();
        if (isBinary(bytes)) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Бинарные файлы не поддерживаются. Загрузите JSON-экспорт сценария.");
        }
        String json = new String(bytes, StandardCharsets.UTF_8);
        ScenarioImportRequest request = objectMapper.readValue(json, ScenarioImportRequest.class);
        return importScenario(request);
    }

    private boolean isBinary(byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0) return true;
        }
        return false;
    }
    @Get("/export/all")
    public List<ScenarioExportDto> exportAll() {
        return scenarioService.findAll().stream()
            .map(s -> {
                try {
                    return new ScenarioExportDto(s, graphService.getGraph(s.id()));
                } catch (HttpStatusException e) {
                    if (e.getStatus() == HttpStatus.NOT_FOUND) {
                        return new ScenarioExportDto(s, null);
                    }
                    throw e;
                }
            })
            .toList();
    }

    @Post("/import/validate")
    public ScenarioImportValidationResult validateImport(@Body ScenarioImportRequest request) {
        List<String> issues = new ArrayList<>();
        if (request == null || request.scenario() == null) {
            return new ScenarioImportValidationResult(false, List.of("scenario is required"));
        }
        if (request.scenario().code() == null || request.scenario().code().isBlank()) {
            issues.add("scenario.code is required");
        }

        boolean codeExists = scenarioService.findAll().stream().anyMatch(s -> s.code().equals(request.scenario().code()));
        if (codeExists) {
            issues.add("scenario.code already exists");
        }

        if (request.graph() != null) {
            if (request.graph().nodes().isEmpty()) {
                issues.add("graph.nodes must not be empty when graph is provided");
            }
            if (request.graph().scenarioId() != null) {
                issues.add("graph.scenarioId must be empty for import payload");
            }
            if (request.graph().version() != 0 && request.graph().version() != 1) {
                issues.add("graph.version is incompatible; expected 0 or 1 for initial import");
            }
        }

        return new ScenarioImportValidationResult(issues.isEmpty(), issues);
    }

    @Post("/import/dry-run")
    public ScenarioImportDryRunResult dryRunImport(@Body ScenarioImportRequest request) {
        ScenarioImportValidationResult validation = validateImport(request);
        if (!validation.valid()) {
            return new ScenarioImportDryRunResult(false, String.join(";", validation.issues()), null, validation.issues().size(), validation.issues());
        }
        return new ScenarioImportDryRunResult(true, "Import payload is valid", request.scenario().code(), 0, List.of());
    }
}
