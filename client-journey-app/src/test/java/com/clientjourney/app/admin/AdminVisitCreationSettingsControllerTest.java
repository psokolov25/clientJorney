package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.VisitCreationSettingsDto;
import com.clientjourney.app.admin.dto.VisitCreationSettingsTestResultDto;
import com.clientjourney.app.service.VisitCreationSettingsService;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdminVisitCreationSettingsControllerTest {

    @Test
    void should_store_load_and_test_settings() {
        AdminVisitCreationSettingsController controller = new AdminVisitCreationSettingsController(new VisitCreationSettingsService());
        UUID id = UUID.randomUUID();

        VisitCreationSettingsDto defaults = controller.get(id);
        assertEquals("DRY_RUN", defaults.provider());

        VisitCreationSettingsDto saved = controller.put(id, new VisitCreationSettingsDto(
            "VISIT_MANAGER",
            "http://visitmanager:8080",
            "ENTRYPOINT_WITH_PARAMETERS",
            Map.of("branchId", "b1")
        ));

        assertEquals("VISIT_MANAGER", saved.provider());
        assertEquals("http://visitmanager:8080", saved.baseUrl());

        VisitCreationSettingsDto reloaded = controller.get(id);
        assertEquals("VISIT_MANAGER", reloaded.provider());
        assertEquals("b1", reloaded.options().get("branchId"));

        VisitCreationSettingsTestResultDto result = controller.test(id);
        assertTrue(result.ok());
        assertTrue(result.message().contains("VISIT_MANAGER"));
    }
}
