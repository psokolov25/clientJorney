package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.VisitCreationSettingsDto;
import com.clientjourney.app.admin.dto.VisitCreationSettingsTestResultDto;
import com.clientjourney.app.service.VisitCreationSettingsService;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Admin API")
@Controller("/api/admin/scenarios/{id}/visit-creation-settings")
public class AdminVisitCreationSettingsController {
    private final VisitCreationSettingsService service;

    public AdminVisitCreationSettingsController(VisitCreationSettingsService service) {
        this.service = service;
    }

    @Operation(summary = "Получить настройки создания визита")
    @Get
    public VisitCreationSettingsDto get(UUID id) {
        return service.get(id);
    }

    @Operation(summary = "Сохранить настройки создания визита")
    @Put
    public VisitCreationSettingsDto put(UUID id, @Body VisitCreationSettingsDto request) {
        return service.put(id, request);
    }

    @Operation(summary = "Проверить настройки создания визита")
    @Post("/test")
    public VisitCreationSettingsTestResultDto test(UUID id) {
        return service.test(id);
    }

    @Operation(summary = "Определить baseUrl VisitManager для отделения")
    @Get("/resolve")
    public Map<String, String> resolve(UUID id, @QueryValue(defaultValue = "") String branchId) {
        return Map.of("branchId", branchId, "baseUrl", service.resolveBaseUrl(id, branchId));
    }
}
