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

    public void registerSession(UUID sessionId, List<SelectedServiceDto> allowedServices, int minSelectedServices, Integer maxSelectedServices) {
        sessions.put(sessionId, new SessionState(allowedServices, minSelectedServices, maxSelectedServices, List.of()));
    }

    public Optional<SessionState> findSession(UUID sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void saveSelectedServices(UUID sessionId, List<SelectedServiceDto> selectedServices) {
        SessionState state = sessions.get(sessionId);
        if (state == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        sessions.put(sessionId, new SessionState(state.allowedServices(), state.minSelectedServices(), state.maxSelectedServices(), selectedServices));
    }

    public record SessionState(
        List<SelectedServiceDto> allowedServices,
        int minSelectedServices,
        Integer maxSelectedServices,
        List<SelectedServiceDto> selectedServices
    ) {
    }
}
