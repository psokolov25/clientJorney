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
        return storage.getOrDefault(scenarioId, new VisitCreationSettingsDto("DRY_RUN", null, Map.of(), "ENTRYPOINT_WITH_PARAMETERS", Map.of()));
    }

    public VisitCreationSettingsDto put(UUID scenarioId, VisitCreationSettingsDto dto) {
        VisitCreationSettingsDto normalized = new VisitCreationSettingsDto(
            dto.provider() == null ? "DRY_RUN" : dto.provider(),
            dto.baseUrl(),
            dto.branchBaseUrls() == null ? Map.of() : dto.branchBaseUrls(),
            dto.mode() == null ? "ENTRYPOINT_WITH_PARAMETERS" : dto.mode(),
            dto.options() == null ? Map.of() : dto.options()
        );
        storage.put(scenarioId, normalized);
        return normalized;
    }

    public VisitCreationSettingsTestResultDto test(UUID scenarioId) {
        VisitCreationSettingsDto settings = get(scenarioId);
        boolean singleServerMode = settings.baseUrl() != null && !settings.baseUrl().isBlank();
        boolean multiServerMode = settings.branchBaseUrls() != null && !settings.branchBaseUrls().isEmpty();

        if (!singleServerMode && !multiServerMode && "VISIT_MANAGER".equalsIgnoreCase(settings.provider())) {
            return new VisitCreationSettingsTestResultDto(false,
                "VISIT_MANAGER requires either baseUrl (single-server mode) or branchBaseUrls (multi-server regional mode)");
        }

        String branchId = settings.options().getOrDefault("branchId", "");
        String resolvedBaseUrl = resolveBaseUrl(settings, branchId);
        String mode = multiServerMode ? "multi-server" : "single-server";

        return new VisitCreationSettingsTestResultDto(true,
            "Settings are valid for provider: " + settings.provider() + " [" + mode + "]"
                + (resolvedBaseUrl == null ? "" : "; resolvedBaseUrl=" + resolvedBaseUrl));
    }

    public String resolveBaseUrl(UUID scenarioId, String branchId) {
        return resolveBaseUrl(get(scenarioId), branchId);
    }

    String resolveBaseUrl(VisitCreationSettingsDto settings, String branchId) {
        if (branchId != null && !branchId.isBlank() && settings.branchBaseUrls() != null) {
            String exact = settings.branchBaseUrls().get(branchId);
            if (exact != null && !exact.isBlank()) {
                return exact;
            }
        }
        if (settings.branchBaseUrls() != null) {
            String wildcard = settings.branchBaseUrls().get("*");
            if (wildcard != null && !wildcard.isBlank()) {
                return wildcard;
            }
        }
        return settings.baseUrl();
    }
}
