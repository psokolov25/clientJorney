package com.clientjourney.app;

import com.clientjourney.app.dto.*;
import com.clientjourney.app.service.*;
import io.micronaut.http.annotation.*;

import java.util.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;


@Tag(name = "Runtime API")
@Controller("/api/runtime")
public class RuntimeController {
    private final RuntimeSessionService runtimeSessionService;
    private final RuntimeAnswerService runtimeAnswerService;
    private final RuntimeCompletionService runtimeCompletionService;
    private final ScenarioService scenarioService;
    private final VisitCreationSettingsService visitCreationSettingsService;

    public RuntimeController(
        RuntimeSessionService runtimeSessionService,
        RuntimeAnswerService runtimeAnswerService,
        RuntimeCompletionService runtimeCompletionService,
        ScenarioService scenarioService,
        VisitCreationSettingsService visitCreationSettingsService
    ) {
        this.runtimeSessionService = runtimeSessionService;
        this.runtimeAnswerService = runtimeAnswerService;
        this.runtimeCompletionService = runtimeCompletionService;
        this.scenarioService = scenarioService;
        this.visitCreationSettingsService = visitCreationSettingsService;
    }

    @Operation(summary = "Запустить runtime-сессию", description = "Запускает runtime-сессию клиентского пути. Режим выбора отделения определяется конфигурацией сценария: BEFORE, AFTER или NONE.")
    @ApiResponse(responseCode = "200", description = "Session started", content = @Content(schema = @Schema(implementation = StartSessionResponse.class)))
    @Post("/scenarios/{scenarioCode}/sessions")
    public StartSessionResponse startSession(@PathVariable String scenarioCode, @Body StartSessionRequest request) {
        String externalUserId = request.externalUserId() == null || request.externalUserId().isBlank()
            ? "anonymous"
            : request.externalUserId();
        String channel = request.channel() == null || request.channel().isBlank() ? "REST" : request.channel();
        return runtimeSessionService.startSession(scenarioCode, channel, externalUserId, request.metadata());
    }

    @Operation(summary = "Конфигурация выбора отделения для runtime", description = "Возвращает режим выбора отделения и список отделений для сценария. Режим: BEFORE/AFTER/NONE.")
    @ApiResponse(responseCode = "200", description = "Branch selection config", content = @Content(schema = @Schema(implementation = BranchSelectionConfigDto.class)))
    @Get("/scenarios/{scenarioCode}/branch-selection-config")
    public BranchSelectionConfigDto branchSelectionConfig(@PathVariable String scenarioCode) {
        UUID scenarioId = scenarioService.findByCode(scenarioCode).id();
        var settings = visitCreationSettingsService.get(scenarioId);

        List<String> branches = settings.branchBaseUrls() == null ? List.of() : settings.branchBaseUrls().keySet().stream()
            .filter(k -> !"*".equals(k)).sorted().toList();

        String timing = settings.options().getOrDefault("branchSelectionTiming", "AUTO").toUpperCase(Locale.ROOT);
        String mode;
        if (branches.size() <= 1) {
            mode = "NONE";
        } else if ("BEFORE".equals(timing) || "AFTER".equals(timing)) {
            mode = timing;
        } else {
            mode = "BEFORE";
        }
        return new BranchSelectionConfigDto(mode, branches);
    }

    @Operation(summary = "Отправить ответ", description = "Отправляет выбранный ответ для активной runtime-сессии.")
    @ApiResponse(responseCode = "200", description = "Answer accepted", content = @Content(schema = @Schema(implementation = StartSessionResponse.class)))
    @Post("/sessions/{sessionId}/answers")
    public StartSessionResponse answer(@PathVariable UUID sessionId, @Body AnswerRequest request) {
        return runtimeAnswerService.processAnswer(sessionId, request);
    }

    @Operation(summary = "Отправить выбранные услуги", description = "Сохраняет выбранные услуги и выполняет попытку создания визита.")
    @ApiResponse(responseCode = "200", description = "Services accepted", content = @Content(schema = @Schema(implementation = StartSessionResponse.class)))
    @Post("/sessions/{sessionId}/selected-services")
    public StartSessionResponse selectServices(@PathVariable UUID sessionId, @Body ServiceSelectionRequest request) {
        return runtimeCompletionService.selectServicesAndComplete(sessionId, request.selectedServices());
    }

    @Operation(summary = "Подтвердить выбранные услуги", description = "Подтверждает ранее выбранные услуги и завершает сессию.")
    @ApiResponse(responseCode = "200", description = "Selection confirmed", content = @Content(schema = @Schema(implementation = StartSessionResponse.class)))
    @Post("/sessions/{sessionId}/selected-services/confirm")
    public StartSessionResponse confirmSelectedServices(@PathVariable UUID sessionId) {
        return runtimeCompletionService.confirmSelectedServices(sessionId);
    }

}
