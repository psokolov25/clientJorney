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
    void should_store_load_and_test_settings_single_and_multi_server_modes() {
        AdminVisitCreationSettingsController controller = new AdminVisitCreationSettingsController(new VisitCreationSettingsService());
        UUID id = UUID.randomUUID();

        VisitCreationSettingsDto defaults = controller.get(id);
        assertEquals("DRY_RUN", defaults.provider());

        VisitCreationSettingsDto saved = controller.put(id, new VisitCreationSettingsDto(
            "VISIT_MANAGER",
            "http://visitmanager:8080",
            Map.of(),
            "ENTRYPOINT_WITH_PARAMETERS",
            Map.of("branchId", "b1")
        ));

        assertEquals("VISIT_MANAGER", saved.provider());
        assertEquals("http://visitmanager:8080", saved.baseUrl());

        VisitCreationSettingsDto reloaded = controller.get(id);
        assertEquals("VISIT_MANAGER", reloaded.provider());
        assertEquals("b1", reloaded.options().get("branchId"));

        VisitCreationSettingsTestResultDto singleResult = controller.test(id);
        assertTrue(singleResult.ok());
        assertTrue(singleResult.message().contains("single-server"));
        assertTrue(singleResult.message().contains("resolvedBaseUrl=http://visitmanager:8080"));

        controller.put(id, new VisitCreationSettingsDto(
            "VISIT_MANAGER",
            null,
            Map.of("north", "http://north-vm:8080", "south", "http://south-vm:8080"),
            "ENTRYPOINT_WITH_PARAMETERS",
            Map.of("branchId", "north")
        ));
        VisitCreationSettingsTestResultDto multiResult = controller.test(id);
        assertTrue(multiResult.ok());
        assertTrue(multiResult.message().contains("multi-server"));
        assertTrue(multiResult.message().contains("resolvedBaseUrl=http://north-vm:8080"));
    }

    @Test
    void should_resolve_branch_base_url() {
        AdminVisitCreationSettingsController controller = new AdminVisitCreationSettingsController(new VisitCreationSettingsService());
        UUID id = UUID.randomUUID();

        controller.put(id, new VisitCreationSettingsDto(
            "VISIT_MANAGER",
            "http://default-vm:8080",
            Map.of("north", "http://north-vm:8080", "*", "http://regional-default-vm:8080"),
            "ENTRYPOINT_WITH_PARAMETERS",
            Map.of()
        ));

        assertEquals("http://north-vm:8080", controller.resolve(id, "north").get("baseUrl"));
        assertEquals("http://regional-default-vm:8080", controller.resolve(id, "west").get("baseUrl"));
    }

}
