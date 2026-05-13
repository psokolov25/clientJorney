package com.clientjourney.channel.telegram;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
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
        assertEquals("COMMAND", input.messageType());
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
    void shouldHandleNullAndMalformedWebhookPayloadsGracefully() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();

        var nullPayloadInput = adapter.fromWebhookUpdate(null, "fallback");
        assertEquals("unknown", nullPayloadInput.externalUserId());
        assertEquals("TEXT", nullPayloadInput.messageType());
        assertEquals("", nullPayloadInput.answerCode());

        var malformedPayloadInput = adapter.fromWebhookUpdate(
                Map.of("message", "unexpected-string"),
                "fallback"
        );
        assertEquals("unknown", malformedPayloadInput.externalUserId());
        assertEquals("TEXT", malformedPayloadInput.messageType());
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
        assertEquals("Markdown", payload.get("parseMode"));
    }

    @Test
    void shouldBuildOutboundPayloadWithReplyMarkupOptions() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        var payload = adapter.buildOutboundPayload("12345", "choose", Map.of(
                "parseMode", "HTML",
                "replyMarkup", Map.of("inline_keyboard", java.util.List.of())
        ));

        assertEquals("HTML", payload.get("parseMode"));
        assertEquals(Map.of("inline_keyboard", java.util.List.of()), payload.get("replyMarkup"));
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

    @Test
    void shouldValidateConfigForPollingAndWebhookModes() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        assertDoesNotThrow(() -> adapter.validateConfig(Map.of(
                "botToken", "token",
                "deliveryMode", "POLLING"
        )));
        assertDoesNotThrow(() -> adapter.validateConfig(Map.of(
                "botToken", "token",
                "deliveryMode", "WEBHOOK",
                "webhookUrl", "https://example.com/tg"
        )));
    }

    @Test
    void shouldMapCallbackAndAttachmentMessageTypes() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();

        var callbackInput = adapter.fromWebhookUpdate(
                Map.of("callback_query", Map.of("from", Map.of("id", 77), "data", "choose_plan")),
                "onboarding"
        );
        assertEquals("CALLBACK", callbackInput.messageType());
        assertEquals("choose_plan", callbackInput.answerCode());

        var attachmentInput = adapter.fromWebhookUpdate(
                Map.of("message", Map.of("from", Map.of("id", 88), "photo", java.util.List.of(Map.of("file_id", "abc")))),
                "onboarding"
        );
        assertEquals("ATTACHMENT", attachmentInput.messageType());
        assertTrue(attachmentInput.metadata().containsKey("attachments"));
    }

    @Test
    void shouldDeduplicatePollingUpdatesByUpdateId() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        Map<String, Object> update = new HashMap<>();
        update.put("update_id", 9001);
        update.put("text", "hello");

        assertTrue(adapter.shouldProcessUpdate(update));
        assertFalse(adapter.shouldProcessUpdate(update));
        assertTrue(adapter.shouldProcessUpdate(Map.of("text", "without-id")));
    }

    @Test
    void shouldFailFastOnInvalidConfig() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();

        IllegalArgumentException missingToken = assertThrows(IllegalArgumentException.class, () ->
                adapter.validateConfig(Map.of("deliveryMode", "POLLING")));
        assertTrue(missingToken.getMessage().contains("CONFIG_MISSING_KEY: botToken"));

        IllegalArgumentException invalidMode = assertThrows(IllegalArgumentException.class, () ->
                adapter.validateConfig(Map.of("botToken", "token", "deliveryMode", "LONG_POLL")));
        assertTrue(invalidMode.getMessage().contains("CONFIG_INVALID_DELIVERY_MODE"));

        IllegalArgumentException missingWebhookUrl = assertThrows(IllegalArgumentException.class, () ->
                adapter.validateConfig(Map.of("botToken", "token", "deliveryMode", "WEBHOOK")));
        assertTrue(missingWebhookUrl.getMessage().contains("CONFIG_MISSING_KEY: webhookUrl"));
    }

    @Test
    void shouldRetryTransientOutboundFailuresAndReturnDeliveryStates() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        var success = adapter.executeOutbound(Map.of(
                "externalUserId", "u-1",
                "text", "hello",
                "simulateTransientFailures", 2,
                "sessionId", "s-1",
                "updateId", "u-42",
                "channelMessageId", "ch-9"
        ), 3);
        assertTrue(success.delivered());
        assertEquals(3, success.attempts());
        assertEquals("sent", success.debug().get("deliveryState"));
        assertEquals(java.util.List.of("queued", "sent"), success.debug().get("deliveryLifecycle"));
        assertEquals(java.util.List.of(250, 500, 1000), success.debug().get("retryScheduleMs"));
        Map<?, ?> successCorrelation = (Map<?, ?>) success.debug().get("correlation");
        assertEquals("s-1", successCorrelation.get("sessionId"));
        assertEquals("u-42", successCorrelation.get("updateId"));

        var failed = adapter.executeOutbound(Map.of(
                "externalUserId", "u-1",
                "text", "hello",
                "simulateTransientFailures", 3
        ), 3);
        assertFalse(failed.delivered());
        assertEquals("RETRY_EXHAUSTED", failed.errorCode());
        assertEquals("failed", failed.debug().get("deliveryState"));
        assertEquals(java.util.List.of("queued", "failed"), failed.debug().get("deliveryLifecycle"));
        assertTrue(failed.debug().containsKey("correlation"));

        Map<String, Object> metrics = adapter.metricsSnapshot();
        assertEquals(2L, metrics.get("outbound.total"));
        assertEquals(1L, metrics.get("outbound.success"));
        assertEquals(1L, metrics.get("outbound.failed"));
        assertEquals(1L, metrics.get("outbound.retryExhausted"));
        assertEquals(4L, metrics.get("outbound.retryAttempts"));
        assertEquals(0.5d, (Double) metrics.get("outbound.errorRate"), 0.0001d);
        assertTrue(((Double) metrics.get("outbound.throughputPerSecond")) > 0d);
        assertTrue(((Long) metrics.get("outbound.latencyP95Ms")) >= 0L);
    }

    @Test
    void shouldNormalizeForcedProviderErrors() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        var temporary = adapter.executeOutbound(Map.of(
                "text", "hello",
                "forceErrorCode", "503",
                "retryBaseDelayMs", 100
        ), 2);
        assertFalse(temporary.delivered());
        assertEquals("TEMPORARY_PROVIDER_ERROR", temporary.errorCode());
        assertEquals(java.util.List.of("queued", "failed"), temporary.debug().get("deliveryLifecycle"));
        assertEquals(java.util.List.of(100, 200), temporary.debug().get("retryScheduleMs"));

        var permanent = adapter.executeOutbound(Map.of(
                "text", "hello",
                "forceErrorCode", "400"
        ), 2);
        assertFalse(permanent.delivered());
        assertEquals("PERMANENT_PROVIDER_ERROR", permanent.errorCode());
    }

    @Test
    void shouldBuildExponentialScheduleWithCapAndJitter() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        var result = adapter.executeOutbound(Map.of(
                "text", "hello",
                "simulateTransientFailures", 10,
                "retryBaseDelayMs", 100,
                "retryMaxDelayMs", 250,
                "retryJitterPercent", 10
        ), 4);
        assertFalse(result.delivered());
        assertEquals(java.util.List.of(110, 220, 250, 250), result.debug().get("retryScheduleMs"));
    }

    @Test
    void shouldExposeUpHealthSnapshotWhenConfigIsValid() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        Map<String, Object> snapshot = adapter.healthSnapshot(Map.of(
                "botToken", "token",
                "deliveryMode", "POLLING"
        ));

        assertEquals("TELEGRAM_POLLING", snapshot.get("adapterType"));
        assertEquals("UP", snapshot.get("status"));
        assertTrue((Boolean) snapshot.get("configValid"));
        assertEquals("", snapshot.get("error"));
        assertTrue(snapshot.get("metrics") instanceof Map<?, ?>);
    }

    @Test
    void shouldExposeDownHealthSnapshotWhenConfigIsInvalid() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        Map<String, Object> snapshot = adapter.healthSnapshot(Map.of("deliveryMode", "POLLING"));

        assertEquals("DOWN", snapshot.get("status"));
        assertFalse((Boolean) snapshot.get("configValid"));
        assertEquals("CONFIG_MISSING_KEY: botToken", snapshot.get("error"));
        assertTrue(snapshot.get("metrics") instanceof Map<?, ?>);
    }

    @Test
    void shouldNormalizeProviderHttpFailuresFor4295xxAndMalformedPayload() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();

        Map<String, Object> tooManyRequests = adapter.normalizeProviderHttpFailure(429, "{\"description\":\"Too Many Requests\"}");
        assertEquals("TEMPORARY_PROVIDER_ERROR", tooManyRequests.get("normalizedCode"));
        assertEquals("Too Many Requests", tooManyRequests.get("detail"));

        Map<String, Object> serverError = adapter.normalizeProviderHttpFailure(503, "{\"error\":\"Service unavailable\"}");
        assertEquals("TEMPORARY_PROVIDER_ERROR", serverError.get("normalizedCode"));
        assertEquals("Service unavailable", serverError.get("detail"));

        Map<String, Object> malformed = adapter.normalizeProviderHttpFailure(400, "{bad-json");
        assertEquals("PERMANENT_PROVIDER_ERROR", malformed.get("normalizedCode"));
        assertEquals("malformed-json", malformed.get("detail"));
    }
}
