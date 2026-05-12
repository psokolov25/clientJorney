package com.clientjourney.app.service;

import com.clientjourney.app.admin.dto.VisitCreationSettingsDto;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisitCreationSettingsServiceTest {

    @Test
    void resolve_base_url_should_prefer_branch_specific_then_fallback_to_default() {
        VisitCreationSettingsService service = new VisitCreationSettingsService();
        UUID id = UUID.randomUUID();

        service.put(id, new VisitCreationSettingsDto(
            "VISIT_MANAGER",
            "http://default-vm:8080",
            Map.of("north", "http://north-vm:8080", "*", "http://regional-default-vm:8080"),
            "ENTRYPOINT_WITH_PARAMETERS",
            Map.of("branchId", "north")
        ));

        VisitCreationSettingsDto settings = service.get(id);
        assertEquals("http://north-vm:8080", service.resolveBaseUrl(settings, "north"));
        assertEquals("http://regional-default-vm:8080", service.resolveBaseUrl(settings, "unknown"));
    }
}
