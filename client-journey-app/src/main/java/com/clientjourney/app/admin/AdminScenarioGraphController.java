package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.GraphValidationResult;
import com.clientjourney.app.admin.dto.CapturePreviewRequest;
import com.clientjourney.app.admin.dto.CapturePreviewResponse;
import com.clientjourney.app.service.CapturePreviewService;
import com.clientjourney.app.service.ScenarioGraphService;
import com.clientjourney.domain.model.ScenarioGraph;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin API")
@Controller("/api/admin/scenarios/{id}")
public class AdminScenarioGraphController {
    private final ScenarioGraphService scenarioGraphService;
    private final CapturePreviewService capturePreviewService;

    public AdminScenarioGraphController(ScenarioGraphService scenarioGraphService, CapturePreviewService capturePreviewService) {
        this.scenarioGraphService = scenarioGraphService;
        this.capturePreviewService = capturePreviewService;
    }

    @Operation(summary = "Получить граф сценария")
    @Get("/graph")
    public HttpResponse<ScenarioGraph> getGraph(UUID id) {
        ScenarioGraph graph = scenarioGraphService.getGraph(id);
        return HttpResponse.ok(graph).header("ETag", "\"" + graph.version() + "\"");
    }

    @Operation(summary = "Сохранить граф сценария")
    @Put("/graph")
    public HttpResponse<ScenarioGraph> putGraph(UUID id,
                                                @Body ScenarioGraph graph,
                                                @Header(value = "If-Match", defaultValue = "") String ifMatch,
                                                @Header(value = "X-Scenario-Graph-Version", defaultValue = "") String scenarioGraphVersion) {
        Integer expectedVersion = resolveExpectedVersion(id, graph.version(), ifMatch, scenarioGraphVersion);
        if (expectedVersion == null) {
            throw new HttpStatusException(HttpStatus.PRECONDITION_REQUIRED,
                "Missing precondition: provide If-Match or X-Scenario-Graph-Version");
        }
        ScenarioGraph saved = scenarioGraphService.saveGraphWithPrecondition(id, graph, expectedVersion);
        return HttpResponse.ok(saved).header("ETag", "\"" + saved.version() + "\"");
    }

    private Integer resolveExpectedVersion(UUID scenarioId, int fallbackVersion, String ifMatch, String scenarioGraphVersion) {
        IfMatchValues ifMatchValues = parseIfMatchValues(ifMatch);
        if (ifMatchValues.hasWildcard || !ifMatchValues.versions.isEmpty()) {
            int currentVersion = resolveCurrentGraphVersionOrFallback(scenarioId, fallbackVersion);
            if (ifMatchValues.hasWildcard) return currentVersion;
            if (ifMatchValues.versions.contains(currentVersion)) return currentVersion;
            return ifMatchValues.versions.get(0);
        }
        if (scenarioGraphVersion != null && !scenarioGraphVersion.isBlank()) {
            try { return Integer.parseInt(scenarioGraphVersion.trim()); } catch (NumberFormatException ignored) { }
        }
        return null;
    }

    private IfMatchValues parseIfMatchValues(String ifMatch) {
        IfMatchValues values = new IfMatchValues();
        if (ifMatch == null || ifMatch.isBlank()) return values;
        String[] etags = ifMatch.split(",");
        for (String etagToken : etags) {
            String sanitized = sanitizeSingleEtagValue(etagToken);
            if (sanitized.isBlank()) continue;
            if ("*".equals(sanitized)) {
                values.hasWildcard = true;
                continue;
            }
            try {
                values.versions.add(Integer.parseInt(sanitized));
            } catch (NumberFormatException ignored) { }
        }
        return values;
    }

    private String sanitizeSingleEtagValue(String ifMatch) {
        String sanitized = ifMatch == null ? "" : ifMatch.trim();
        if (sanitized.startsWith("W/")) sanitized = sanitized.substring(2);
        return sanitized.replace("\"", "").trim();
    }

    private static final class IfMatchValues {
        private final List<Integer> versions = new ArrayList<>();
        private boolean hasWildcard;
    }

    private int resolveCurrentGraphVersionOrFallback(UUID scenarioId, int fallbackVersion) {
        try {
            return scenarioGraphService.getGraph(scenarioId).version();
        } catch (HttpStatusException ex) {
            if (ex.getStatus() == HttpStatus.NOT_FOUND) {
                return fallbackVersion;
            }
            throw ex;
        }
    }

    @Operation(summary = "Проверить граф сценария")
    @Post("/validate")
    public GraphValidationResult validate(UUID id) {
        return scenarioGraphService.validateGraph(id);
    }

    @Operation(summary = "Dry-run capture-узла графа")
    @Post("/graph/capture-preview")
    public CapturePreviewResponse capturePreview(UUID id, @Body CapturePreviewRequest request) {
        ScenarioGraph graph = scenarioGraphService.getGraph(id);
        return capturePreviewService.preview(graph, request);
    }
}
