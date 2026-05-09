package com.clientjourney.app.service;

import com.clientjourney.app.dto.AnswerRequest;
import com.clientjourney.app.dto.OutputMessage;
import com.clientjourney.app.dto.StartSessionResponse;
import jakarta.inject.Singleton;

import java.util.UUID;

@Singleton
public class RuntimeAnswerService {
    private final ConversationSessionService conversationSessionService;

    public RuntimeAnswerService(ConversationSessionService conversationSessionService) {
        this.conversationSessionService = conversationSessionService;
    }

    public StartSessionResponse processAnswer(UUID sessionId, AnswerRequest request) {
        conversationSessionService.findSession(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        String text = "Answer accepted: " + (request.answerCode() == null ? "unknown" : request.answerCode());
        return StartSessionResponse.withoutVisitCreation(
            sessionId.toString(),
            null,
            "ACTIVE",
            OutputMessage.info(text)
        );
    }
}
