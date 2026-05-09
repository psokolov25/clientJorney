package com.clientjourney.app.service;

import com.clientjourney.app.dto.OutputMessage;
import com.clientjourney.app.dto.SelectedServiceDto;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class ServiceSelectionProcessor {
    private final ConversationSessionService conversationSessionService;

    public ServiceSelectionProcessor(ConversationSessionService conversationSessionService) {
        this.conversationSessionService = conversationSessionService;
    }

    public OutputMessage processSelectedServices(UUID sessionId, List<SelectedServiceDto> selectedServices) {
        List<SelectedServiceDto> safeServices = selectedServices == null ? List.of() : selectedServices;
        ConversationSessionService.SessionState state = conversationSessionService.findSession(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        validateSelection(safeServices, state);
        conversationSessionService.saveSelectedServices(sessionId, safeServices);
        return OutputMessage.result("Service selection accepted. Selected services: " + safeServices.size());
    }

    private void validateSelection(List<SelectedServiceDto> selectedServices, ConversationSessionService.SessionState state) {
        int min = state.minSelectedServices();
        Integer max = state.maxSelectedServices();

        if (selectedServices.size() < min) {
            throw new IllegalArgumentException("Minimum selected services is " + min);
        }
        if (max != null && selectedServices.size() > max) {
            throw new IllegalArgumentException("Maximum selected services is " + max);
        }

        Set<String> allowedIds = state.allowedServices().stream().map(SelectedServiceDto::serviceId).collect(Collectors.toSet());
        Set<String> uniq = new HashSet<>();
        for (SelectedServiceDto selectedService : selectedServices) {
            if (selectedService == null || selectedService.serviceId() == null || selectedService.serviceId().isBlank()) {
                throw new IllegalArgumentException("Selected service must have non-empty serviceId");
            }
            if (!allowedIds.contains(selectedService.serviceId())) {
                throw new IllegalArgumentException("Service is not allowed: " + selectedService.serviceId());
            }
            if (!uniq.add(selectedService.serviceId())) {
                throw new IllegalArgumentException("Duplicate selected service: " + selectedService.serviceId());
            }
        }
    }
}
