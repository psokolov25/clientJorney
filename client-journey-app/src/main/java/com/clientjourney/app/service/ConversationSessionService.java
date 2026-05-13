package com.clientjourney.app.service;

import com.clientjourney.app.dto.SelectedServiceDto;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ConversationSessionService {
    private final Map<UUID, SessionState> sessions = new ConcurrentHashMap<>();

    public void registerSession(
        UUID sessionId,
        String scenarioCode,
        String channel,
        String externalUserId,
        Map<String, Object> metadata,
        List<SelectedServiceDto> allowedServices,
        int minSelectedServices,
        Integer maxSelectedServices
    ) {
        sessions.put(sessionId, new SessionState(scenarioCode, channel, externalUserId, metadata, allowedServices, minSelectedServices, maxSelectedServices, List.of()));
    }

    public Optional<SessionState> findSession(UUID sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void updateCurrentNodeId(UUID sessionId, String currentNodeId) {
        SessionState state = sessions.get(sessionId);
        if (state == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        Map<String, Object> updatedMetadata = new ConcurrentHashMap<>(state.metadata());
        updatedMetadata.put("currentNodeId", currentNodeId);
        sessions.put(sessionId, new SessionState(
            state.scenarioCode(),
            state.channel(),
            state.externalUserId(),
            updatedMetadata,
            state.allowedServices(),
            state.minSelectedServices(),
            state.maxSelectedServices(),
            state.selectedServices()
        ));
    }

    public void saveSelectedServices(UUID sessionId, List<SelectedServiceDto> selectedServices) {
        SessionState state = sessions.get(sessionId);
        if (state == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        sessions.put(sessionId, new SessionState(
            state.scenarioCode(),
            state.channel(),
            state.externalUserId(),
            state.metadata(),
            state.allowedServices(),
            state.minSelectedServices(),
            state.maxSelectedServices(),
            selectedServices
        ));
    }

    public void mergeMetadata(UUID sessionId, Map<String, Object> additionalMetadata) {
        SessionState state = sessions.get(sessionId);
        if (state == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        Map<String, Object> updatedMetadata = new ConcurrentHashMap<>(state.metadata());
        if (additionalMetadata != null) {
            updatedMetadata.putAll(additionalMetadata);
        }
        sessions.put(sessionId, new SessionState(
            state.scenarioCode(),
            state.channel(),
            state.externalUserId(),
            updatedMetadata,
            state.allowedServices(),
            state.minSelectedServices(),
            state.maxSelectedServices(),
            state.selectedServices()
        ));
    }

    public record SessionState(
        String scenarioCode,
        String channel,
        String externalUserId,
        Map<String, Object> metadata,
        List<SelectedServiceDto> allowedServices,
        int minSelectedServices,
        Integer maxSelectedServices,
        List<SelectedServiceDto> selectedServices
    ) {
    }
}
