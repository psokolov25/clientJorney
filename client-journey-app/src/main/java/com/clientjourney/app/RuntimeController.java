package com.clientjourney.app;

import com.clientjourney.app.dto.OutputMessage;
import com.clientjourney.app.dto.StartSessionRequest;
import com.clientjourney.app.dto.StartSessionResponse;
import com.clientjourney.core.ScenarioEngine;
import com.clientjourney.core.SessionStartResult;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;

@Controller("/api/runtime")
public class RuntimeController {
    private final ScenarioEngine scenarioEngine;

    public RuntimeController(ScenarioEngine scenarioEngine) {
        this.scenarioEngine = scenarioEngine;
    }

    @Post("/scenarios/{scenarioCode}/sessions")
    public StartSessionResponse startSession(@PathVariable String scenarioCode, @Body StartSessionRequest request) {
        String externalUserId = request.externalUserId() == null || request.externalUserId().isBlank()
                ? "anonymous"
                : request.externalUserId();
        SessionStartResult result = scenarioEngine.startSession(scenarioCode, externalUserId);
        return new StartSessionResponse(
                result.sessionId(),
                result.scenarioCode(),
                result.status(),
                new OutputMessage("INFO", "Session started for user " + result.externalUserId())
        );
    }
}
