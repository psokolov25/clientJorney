package com.clientjourney.app;

import com.clientjourney.app.dto.AnswerRequest;
import com.clientjourney.app.dto.ServiceSelectionRequest;
import com.clientjourney.app.dto.StartSessionRequest;
import com.clientjourney.app.dto.StartSessionResponse;
import com.clientjourney.app.service.RuntimeAnswerService;
import com.clientjourney.app.service.RuntimeCompletionService;
import com.clientjourney.app.service.RuntimeSessionService;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;

import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Runtime API")
@Controller("/api/runtime")
public class RuntimeController {
    private final RuntimeSessionService runtimeSessionService;
    private final RuntimeAnswerService runtimeAnswerService;
    private final RuntimeCompletionService runtimeCompletionService;

    public RuntimeController(
        RuntimeSessionService runtimeSessionService,
        RuntimeAnswerService runtimeAnswerService,
        RuntimeCompletionService runtimeCompletionService
    ) {
        this.runtimeSessionService = runtimeSessionService;
        this.runtimeAnswerService = runtimeAnswerService;
        this.runtimeCompletionService = runtimeCompletionService;
    }

    @Operation(summary = "Start runtime session")
    @Post("/scenarios/{scenarioCode}/sessions")
    public StartSessionResponse startSession(@PathVariable String scenarioCode, @Body StartSessionRequest request) {
        String externalUserId = request.externalUserId() == null || request.externalUserId().isBlank()
            ? "anonymous"
            : request.externalUserId();
        String channel = request.channel() == null || request.channel().isBlank() ? "REST" : request.channel();
        return runtimeSessionService.startSession(scenarioCode, channel, externalUserId, request.metadata());
    }

    @Operation(summary = "Submit answer")
    @Post("/sessions/{sessionId}/answers")
    public StartSessionResponse answer(@PathVariable UUID sessionId, @Body AnswerRequest request) {
        return runtimeAnswerService.processAnswer(sessionId, request);
    }

    @Operation(summary = "Submit selected services")
    @Post("/sessions/{sessionId}/selected-services")
    public StartSessionResponse selectServices(@PathVariable UUID sessionId, @Body ServiceSelectionRequest request) {
        return runtimeCompletionService.selectServicesAndComplete(sessionId, request.selectedServices());
    }

    @Operation(summary = "Confirm selected services")
    @Post("/sessions/{sessionId}/selected-services/confirm")
    public StartSessionResponse confirmSelectedServices(@PathVariable UUID sessionId) {
        return runtimeCompletionService.confirmSelectedServices(sessionId);
    }

}
