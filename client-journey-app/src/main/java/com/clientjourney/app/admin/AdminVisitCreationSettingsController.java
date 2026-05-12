package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.VisitCreationSettingsDto;
import com.clientjourney.app.admin.dto.VisitCreationSettingsTestResultDto;
import com.clientjourney.app.service.VisitCreationSettingsService;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Admin API")
@Controller("/api/admin/scenarios/{id}/visit-creation-settings")
public class AdminVisitCreationSettingsController {
    private final VisitCreationSettingsService service;

    public AdminVisitCreationSettingsController(VisitCreationSettingsService service) {
        this.service = service;
    }

    @Operation(summary = "Get visit creation settings")
    @Get
    public VisitCreationSettingsDto get(UUID id) {
        return service.get(id);
    }

    @Operation(summary = "Save visit creation settings")
    @Put
    public VisitCreationSettingsDto put(UUID id, @Body VisitCreationSettingsDto request) {
        return service.put(id, request);
    }

    @Operation(summary = "Test visit creation settings")
    @Post("/test")
    public VisitCreationSettingsTestResultDto test(UUID id) {
        return service.test(id);
    }
}
