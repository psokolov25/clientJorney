package com.clientjourney.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmbeddedFrontendResourcesTest {

    @Test
    void embedded_admin_chat_and_root_resources_should_exist() {
        assertNotNull(getClass().getResource("/public/index.html"));

        assertNotNull(getClass().getResource("/public/admin/index.html"));
        assertNotNull(getClass().getResource("/public/admin/i18n/en.json"));
        assertNotNull(getClass().getResource("/public/admin/i18n/ru.json"));

        assertNotNull(getClass().getResource("/public/chat/index.html"));
        assertNotNull(getClass().getResource("/public/chat/i18n/en.json"));
        assertNotNull(getClass().getResource("/public/chat/i18n/ru.json"));
    }
}
