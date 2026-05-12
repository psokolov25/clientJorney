package com.clientjourney.channel.telegram;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TelegramPollingAdapterSkeletonTest {
    @Test
    void shouldExposeAdapterMetadata() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        assertEquals("TELEGRAM_POLLING", adapter.adapterType());
        assertEquals("PARTIALLY_IMPLEMENTED", adapter.status());
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
    void shouldMapTelegramNativeWebhookShape() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        var input = adapter.fromWebhookUpdate(
                Map.of("message", Map.of("from", Map.of("id", 777), "text", "hello")),
                "onboarding"
        );

        assertEquals("777", input.externalUserId());
        assertEquals("hello", input.answerCode());
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

    @Test
    void shouldBuildPollingAndWebhookPayloads() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        var polling = adapter.buildPollingFetchPayload(42, 25);
        var webhook = adapter.buildWebhookRegistrationPayload("https://example.com/hook", "secret-token");

        assertEquals("GET", polling.get("method"));
        assertEquals(42L, polling.get("offset"));
        assertEquals(25, polling.get("timeout"));

        assertEquals("POST", webhook.get("method"));
        assertEquals("https://example.com/hook", webhook.get("url"));
        assertEquals("secret-token", webhook.get("secretToken"));
    }

    @Test
    void shouldValidateHmacSignature() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        String payload = "{\"ok\":true}";
        String secret = "s3cr3t";
        String signature = "629c5b4f3ca50d22a893a236367a715cf8148cbf7a749829c7d2eaf89ea74039";

        assertTrue(adapter.validateWebhookSignature(payload, signature, secret));
        assertFalse(adapter.validateWebhookSignature(payload, "bad", secret));
    }
}
