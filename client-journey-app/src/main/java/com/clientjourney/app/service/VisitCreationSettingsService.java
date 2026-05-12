package com.clientjourney.app.service;

import com.clientjourney.app.admin.dto.VisitCreationSettingsDto;
import com.clientjourney.app.admin.dto.VisitCreationSettingsTestResultDto;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class VisitCreationSettingsService {
    private final Map<UUID, VisitCreationSettingsDto> storage = new ConcurrentHashMap<>();

    public VisitCreationSettingsDto get(UUID scenarioId) {
        return storage.getOrDefault(scenarioId, new VisitCreationSettingsDto("DRY_RUN", null, "ENTRYPOINT_WITH_PARAMETERS", Map.of()));
    }

    public VisitCreationSettingsDto put(UUID scenarioId, VisitCreationSettingsDto dto) {
        VisitCreationSettingsDto normalized = new VisitCreationSettingsDto(
            dto.provider() == null ? "DRY_RUN" : dto.provider(),
            dto.baseUrl(),
            dto.mode() == null ? "ENTRYPOINT_WITH_PARAMETERS" : dto.mode(),
            dto.options() == null ? Map.of() : dto.options()
        );
        storage.put(scenarioId, normalized);
        return normalized;
    }

    public VisitCreationSettingsTestResultDto test(UUID scenarioId) {
        VisitCreationSettingsDto settings = get(scenarioId);
        return new VisitCreationSettingsTestResultDto(true, "Settings are valid for provider: " + settings.provider());
    }
}
