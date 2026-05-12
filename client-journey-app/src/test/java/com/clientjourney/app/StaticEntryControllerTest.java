package com.clientjourney.app;

import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticEntryControllerTest {

    private final StaticEntryController controller = new StaticEntryController();

    @Test
    void shouldServeRootAdminAndChatPages() {
        var root = controller.root();
        var admin = controller.admin();
        var chat = controller.chat();

        assertEquals(HttpStatus.OK, root.getStatus());
        assertEquals(HttpStatus.OK, admin.getStatus());
        assertEquals(HttpStatus.OK, chat.getStatus());
        assertTrue(root.body().contains("<!doctype html") || root.body().contains("<html"));
    }

    @Test
    void shouldServeSwaggerUiAndSpec() {
        var ui = controller.swaggerUi();
        var spec = controller.swaggerFile("client-journey-platform-1.0.0.yml");

        assertEquals(HttpStatus.OK, ui.getStatus());
        assertEquals(HttpStatus.OK, spec.getStatus());
        assertTrue(ui.body().contains("SwaggerUIBundle"));
        assertTrue(spec.body().contains("openapi:"));
    }
}
