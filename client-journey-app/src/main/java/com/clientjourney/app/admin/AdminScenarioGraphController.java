package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.GraphValidationResult;
import com.clientjourney.app.service.ScenarioGraphService;
import com.clientjourney.domain.model.ScenarioGraph;
import io.micronaut.http.annotation.*;

import java.util.UUID;

@Controller("/api/admin/scenarios/{id}")
public class AdminScenarioGraphController {
    private final ScenarioGraphService scenarioGraphService;

    public AdminScenarioGraphController(ScenarioGraphService scenarioGraphService) {
        this.scenarioGraphService = scenarioGraphService;
    }

    @Get("/graph")
    public ScenarioGraph getGraph(UUID id) {
        return scenarioGraphService.getGraph(id);
    }

    @Put("/graph")
    public ScenarioGraph putGraph(UUID id, @Body ScenarioGraph graph) {
        return scenarioGraphService.saveGraph(id, graph);
    }

    @Post("/validate")
    public GraphValidationResult validate(UUID id) {
        return scenarioGraphService.validateGraph(id);
    }
}
