package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.GraphValidationResult;
import com.clientjourney.app.service.ScenarioGraphService;
import com.clientjourney.domain.model.ScenarioGraph;
import io.micronaut.http.annotation.*;

import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin API")
@Controller("/api/admin/scenarios/{id}")
public class AdminScenarioGraphController {
    private final ScenarioGraphService scenarioGraphService;

    public AdminScenarioGraphController(ScenarioGraphService scenarioGraphService) {
        this.scenarioGraphService = scenarioGraphService;
    }

    @Operation(summary = "Получить граф сценария")
    @Get("/graph")
    public ScenarioGraph getGraph(UUID id) {
        return scenarioGraphService.getGraph(id);
    }

    @Operation(summary = "Сохранить граф сценария")
    @Put("/graph")
    public ScenarioGraph putGraph(UUID id, @Body ScenarioGraph graph) {
        return scenarioGraphService.saveGraph(id, graph);
    }

    @Operation(summary = "Проверить граф сценария")
    @Post("/validate")
    public GraphValidationResult validate(UUID id) {
        return scenarioGraphService.validateGraph(id);
    }
}
