package com.clientjourney.visit.custom;

import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomRestVisitCreationClientTest {

    @Test
    void shouldReturnSkippedWhenDisabled() {
        CustomRestVisitCreationClient client = new CustomRestVisitCreationClient(false, "http://api.local");

        var result = client.createVisit(new VisitCreationRequest(UUID.randomUUID(), "scenario", 1, "web", "u", Map.of()));

        assertEquals(VisitCreationStatus.SKIPPED, result.status());
        assertEquals("DISABLED", result.errorCode());
    }

    @Test
    void shouldReturnFailedNotImplementedWhenEnabled() {
        CustomRestVisitCreationClient client = new CustomRestVisitCreationClient(true, "http://api.local");

        var result = client.createVisit(new VisitCreationRequest(UUID.randomUUID(), "scenario", 1, "web", "u", Map.of()));

        assertEquals(VisitCreationStatus.FAILED, result.status());
        assertEquals("NOT_IMPLEMENTED", result.errorCode());
        assertEquals("CUSTOM_REST", result.clientType());
    }
}
