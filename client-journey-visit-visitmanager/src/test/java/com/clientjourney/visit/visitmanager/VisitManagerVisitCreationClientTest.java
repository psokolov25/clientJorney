package com.clientjourney.visit.visitmanager;

import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VisitManagerVisitCreationClientTest {

    @Test
    void shouldReturnSkippedWhenDisabled() {
        VisitManagerVisitCreationClient client = new VisitManagerVisitCreationClient(false, "ENTRYPOINT_WITH_PARAMETERS");

        var result = client.createVisit(new VisitCreationRequest(UUID.randomUUID(), "scenario", 1, "web", "user", Map.of()));

        assertEquals(VisitCreationStatus.SKIPPED, result.status());
        assertEquals("DISABLED", result.errorCode());
        assertNull(result.externalVisitId());
    }

    @Test
    void shouldBuildVisitAndTicketWhenEnabled() {
        UUID sessionId = UUID.randomUUID();
        VisitManagerVisitCreationClient client = new VisitManagerVisitCreationClient(true, "ENTRYPOINT_WITH_PARAMETERS");

        var result = client.createVisit(new VisitCreationRequest(
            sessionId,
            "scenario-a",
            1,
            "web",
            "user-1",
            Map.of("selectedServicesCount", "2")
        ));

        assertEquals(VisitCreationStatus.SUCCESS, result.status());
        assertEquals("vm-" + sessionId, result.externalVisitId());
        assertEquals("ENTRYPOINT_WITH_PARAMETERS-scenario-a-2-web", result.externalTicket());
    }
}
