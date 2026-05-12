package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.CreateScenarioRequest;
import com.clientjourney.app.admin.dto.UpdateScenarioRequest;
import com.clientjourney.app.service.ScenarioService;
import com.clientjourney.domain.model.Scenario;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;

import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin API")
@Controller("/api/admin/scenarios")
public class AdminScenarioController {
    private final ScenarioService scenarioService;

    public AdminScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @Operation(summary = "Список сценариев")
    @Get
    public List<Scenario> findAll() {
        return scenarioService.findAll();
    }

    @Operation(summary = "Создать сценарий")
    @Post
    @Status(HttpStatus.CREATED)
    public Scenario create(@Body CreateScenarioRequest request) {
        return scenarioService.create(request);
    }

    @Operation(summary = "Получить сценарий по id")
    @Get("/{id}")
    public Scenario findById(UUID id) {
        return scenarioService.findById(id);
    }

    @Operation(summary = "Обновить сценарий")
    @Put("/{id}")
    public Scenario update(UUID id, @Body UpdateScenarioRequest request) {
        return scenarioService.update(id, request);
    }

    @Operation(summary = "Удалить сценарий")
    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    public void delete(UUID id) {
        scenarioService.delete(id);
    }

    @Post("/{id}/publish")
    public Scenario publish(UUID id) {
        return scenarioService.publish(id);
    }

    @Post("/{id}/archive")
    public Scenario archive(UUID id) {
        return scenarioService.archive(id);
    }

    @Post("/{id}/clone-version")
    public Scenario cloneVersion(UUID id) {
        return scenarioService.cloneVersion(id);
    }
}
