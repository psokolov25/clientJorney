package com.clientjourney.channel.telegram;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TelegramPollingAdapterSkeletonTest {
    @Test
    void shouldExposeAdapterMetadata() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        assertEquals("TELEGRAM_POLLING", adapter.adapterType());
        assertEquals("NOT_IMPLEMENTED", adapter.status());
        assertEquals(java.util.List.of("POLLING", "WEBHOOK"), adapter.supportedDeliveryModes());
        assertEquals(java.util.List.of("botToken", "deliveryMode"), adapter.requiredConfigKeys());
        assertTrue(adapter.supportsWebhookSignatureValidation());
        assertTrue(adapter.supportsLongPolling());
    }

    @Test
    void shouldMapWebhookUpdateToClientInputMessage() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        var input = adapter.fromWebhookUpdate(Map.of("fromId", 12345, "text", "/start"), "medical-registration");

        assertEquals("TELEGRAM", input.channel());
        assertEquals("12345", input.externalUserId());
        assertEquals("TEXT", input.messageType());
        assertEquals("/start", input.answerCode());
        assertEquals("/start", input.answerValue());
        assertEquals("medical-registration", input.scenarioCode());
        assertNotNull(input.sessionId());
    }

    @Test
    void buildsOutboundPayloadContract() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        var payload = adapter.buildOutboundPayload("12345", "hello");

        assertEquals("POST", payload.get("method"));
        assertEquals("https://api.telegram.org/bot{token}/sendMessage", payload.get("urlTemplate"));
        assertEquals("token-in-url", payload.get("auth"));
        assertEquals("12345", payload.get("externalUserId"));
        assertEquals("hello", payload.get("text"));
    }
}
