package com.clientjourney.app.service;

import com.clientjourney.app.admin.dto.GraphValidationResult;
import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.storage.spi.ScenarioGraphRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;

@Singleton
public class ScenarioGraphService {
    private final ScenarioService scenarioService;
    private final ScenarioGraphRepository scenarioGraphRepository;
    private final RouteValidationService routeValidationService;

    public ScenarioGraphService(ScenarioService scenarioService,
                                ScenarioGraphRepository scenarioGraphRepository,
                                RouteValidationService routeValidationService) {
        this.scenarioService = scenarioService;
        this.scenarioGraphRepository = scenarioGraphRepository;
        this.routeValidationService = routeValidationService;
    }

    public ScenarioGraph getGraph(java.util.UUID scenarioId) {
        Scenario scenario = scenarioService.findById(scenarioId);
        return scenarioGraphRepository.findByScenarioIdAndVersion(scenarioId, scenario.version())
            .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Scenario graph not found: " + scenarioId));
    }

    public ScenarioGraph saveGraph(java.util.UUID scenarioId, ScenarioGraph graph) {
        return saveGraphWithPrecondition(scenarioId, graph, null);
    }

    public ScenarioGraph saveGraphWithPrecondition(java.util.UUID scenarioId, ScenarioGraph graph, Integer expectedVersion) {
        Scenario scenario = scenarioService.findById(scenarioId);
        if (expectedVersion != null && expectedVersion != scenario.version()) {
            throw new HttpStatusException(HttpStatus.CONFLICT,
                "Scenario graph version conflict: expected " + expectedVersion + ", actual " + scenario.version());
        }
        ScenarioGraph normalized = new ScenarioGraph(scenarioId, scenario.version(), graph.nodes(), graph.edges());
        return scenarioGraphRepository.save(normalized);
    }

    public GraphValidationResult validateGraph(java.util.UUID scenarioId) {
        return routeValidationService.validate(getGraph(scenarioId));
    }
}
