package com.clientjourney.app;

import com.clientjourney.app.dto.AnswerRequest;
import com.clientjourney.app.dto.RuntimeWsRequest;
import com.clientjourney.app.dto.RuntimeWsResponse;
import com.clientjourney.app.dto.ServiceSelectionRequest;
import com.clientjourney.app.dto.StartSessionRequest;
import com.clientjourney.app.dto.StartSessionResponse;
import com.clientjourney.app.service.RuntimeAnswerService;
import com.clientjourney.app.service.RuntimeSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@ServerWebSocket("/ws/runtime")
public class RuntimeWebSocket {
    private final RuntimeSessionService runtimeSessionService;
    private final RuntimeAnswerService runtimeAnswerService;
    private final com.clientjourney.app.service.RuntimeCompletionService runtimeCompletionService;
    private final ObjectMapper objectMapper;

    public RuntimeWebSocket(
        RuntimeSessionService runtimeSessionService,
        RuntimeAnswerService runtimeAnswerService,
        com.clientjourney.app.service.RuntimeCompletionService runtimeCompletionService,
        ObjectMapper objectMapper
    ) {
        this.runtimeSessionService = runtimeSessionService;
        this.runtimeAnswerService = runtimeAnswerService;
        this.runtimeCompletionService = runtimeCompletionService;
        this.objectMapper = objectMapper;
    }

    @OnMessage
    public void onMessage(String payload, WebSocketSession session) throws IOException {
        RuntimeWsResponse response;
        try {
            RuntimeWsRequest request = objectMapper.readValue(payload, RuntimeWsRequest.class);
            response = RuntimeWsResponse.ok(handle(request));
        } catch (IllegalArgumentException e) {
            response = RuntimeWsResponse.error(e.getMessage());
        } catch (Exception e) {
            response = RuntimeWsResponse.error("Unsupported runtime message");
        }
        session.sendSync(objectMapper.writeValueAsString(response));
    }

    StartSessionResponse handle(RuntimeWsRequest request) {
        if ("startSession".equals(request.action())) {
            String scenarioCode = request.scenarioCode();
            if (scenarioCode == null || scenarioCode.isBlank()) {
                throw new IllegalArgumentException("scenarioCode is required");
            }
            StartSessionRequest start = new StartSessionRequest(
                request.channel() == null ? "WS" : request.channel(),
                request.externalUserId(),
                request.metadata() == null ? Map.of() : request.metadata()
            );
            String externalUserId = start.externalUserId() == null || start.externalUserId().isBlank() ? "anonymous" : start.externalUserId();
            return runtimeSessionService.startSession(scenarioCode, start.channel(), externalUserId, start.metadata());
        }

        if ("answer".equals(request.action())) {
            if (request.sessionId() == null || request.sessionId().isBlank()) {
                throw new IllegalArgumentException("sessionId is required");
            }
            return runtimeAnswerService.processAnswer(
                UUID.fromString(request.sessionId()),
                new AnswerRequest(request.answerCode(), request.answerValue())
            );
        }


        if ("confirmServices".equals(request.action())) {
            if (request.sessionId() == null || request.sessionId().isBlank()) {
                throw new IllegalArgumentException("sessionId is required");
            }
            return runtimeCompletionService.selectServicesAndComplete(
                UUID.fromString(request.sessionId()),
                request.selectedServices()
            );
        }

        throw new IllegalArgumentException("Unknown action: " + request.action());
    }
}
