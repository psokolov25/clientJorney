package com.clientjourney.app.service;

import com.clientjourney.app.dto.SelectedServiceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceSelectionProcessorTest {
    private ConversationSessionService conversationSessionService;
    private ServiceSelectionProcessor processor;
    private UUID sessionId;
    private SelectedServiceDto svc1;
    private SelectedServiceDto svc2;

    @BeforeEach
    void setUp() {
        conversationSessionService = new ConversationSessionService();
        processor = new ServiceSelectionProcessor(conversationSessionService);
        sessionId = UUID.randomUUID();
        svc1 = new SelectedServiceDto("svc-1", "S1", "Service 1");
        svc2 = new SelectedServiceDto("svc-2", "S2", "Service 2");
    }

    @Test
    void shouldAcceptValidSelectionAndPersistIt() {
        conversationSessionService.registerSession(sessionId, "scenario-a", "REST", "user-1", java.util.Map.of(), List.of(svc1, svc2), 1, 2);

        var output = processor.processSelectedServices(sessionId, List.of(svc1));

        assertEquals("RESULT", output.type());
        assertTrue(output.text().contains("Selected services: 1"));
        assertEquals(1, ((java.util.List<?>) output.payload().get("selectedServices")).size());
        assertEquals(1, output.payload().get("selectedCount"));
        var state = conversationSessionService.findSession(sessionId).orElseThrow();
        assertEquals(1, state.selectedServices().size());
        assertEquals("svc-1", state.selectedServices().get(0).serviceId());
    }

    @Test
    void shouldRejectWhenSelectionCountIsLessThanMin() {
        conversationSessionService.registerSession(sessionId, "scenario-a", "REST", "user-1", java.util.Map.of(), List.of(svc1, svc2), 1, 2);

        var ex = assertThrows(IllegalArgumentException.class,
            () -> processor.processSelectedServices(sessionId, List.of()));

        assertEquals("Minimum selected services is 1", ex.getMessage());
    }

    @Test
    void shouldRejectServiceThatIsNotAllowed() {
        conversationSessionService.registerSession(sessionId, "scenario-a", "REST", "user-1", java.util.Map.of(), List.of(svc1), 0, 2);
        SelectedServiceDto notAllowed = new SelectedServiceDto("svc-x", "SX", "Other");

        var ex = assertThrows(IllegalArgumentException.class,
            () -> processor.processSelectedServices(sessionId, List.of(notAllowed)));

        assertEquals("Service is not allowed: svc-x", ex.getMessage());
    }

    @Test
    void shouldRejectDuplicateServices() {
        conversationSessionService.registerSession(sessionId, "scenario-a", "REST", "user-1", java.util.Map.of(), List.of(svc1, svc2), 0, 2);

        var ex = assertThrows(IllegalArgumentException.class,
            () -> processor.processSelectedServices(sessionId, List.of(svc1, svc1)));

        assertEquals("Duplicate selected service: svc-1", ex.getMessage());
    }
}
