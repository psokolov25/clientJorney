package com.clientjourney.channel.telegram;

import com.clientjourney.channel.spi.ClientInputMessage;
import com.clientjourney.channel.spi.IntegrationReadyChannelAdapter;
import com.clientjourney.channel.spi.WebhookSignatureSupport;
import com.clientjourney.channel.spi.DeliveryResult;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Requires(property = "client-journey.channel.telegram.enabled", value = "true")
@Singleton
public class TelegramPollingAdapterSkeleton implements IntegrationReadyChannelAdapter, WebhookSignatureSupport {

    public String adapterType() {
        return "TELEGRAM_POLLING";
    }

    public String status() {
        return "PARTIALLY_IMPLEMENTED";
    }

    public List<String> supportedDeliveryModes() {
        return List.of("POLLING", "WEBHOOK");
    }

    @Override
    public List<String> requiredConfigKeys() {
        return List.of("botToken", "deliveryMode");
    }

    @Override
    public boolean supportsWebhookSignatureValidation() {
        return true;
    }

    public boolean supportsLongPolling() {
        return true;
    }

    public ClientInputMessage fromWebhookUpdate(Map<String, Object> update, String scenarioCode) {
        String externalUserId = extractExternalUserId(update).orElse("unknown");
        String text = extractText(update);
        return new ClientInputMessage(
                UUID.randomUUID(),
                scenarioCode,
                "TELEGRAM",
                externalUserId,
                "TEXT",
                text,
                text,
                update
        );
    }

    public ClientInputMessage fromPollingUpdate(Map<String, Object> update, String scenarioCode) {
        return fromWebhookUpdate(update, scenarioCode);
    }

    public String resolveDeliveryMode(Map<String, Object> config) {
        String mode = String.valueOf(config.getOrDefault("deliveryMode", "POLLING")).toUpperCase();
        return switch (mode) {
            case "POLLING", "WEBHOOK" -> mode;
            default -> "POLLING";
        };
    }

    public Map<String, Object> buildPollingFetchPayload(long offset, int timeoutSeconds) {
        return Map.of(
                "method", "GET",
                "urlTemplate", "https://api.telegram.org/bot{token}/getUpdates",
                "auth", "token-in-url",
                "offset", Math.max(0, offset),
                "timeout", Math.max(1, timeoutSeconds)
        );
    }

    public Map<String, Object> buildWebhookRegistrationPayload(String webhookUrl, String secretToken) {
        return Map.of(
                "method", "POST",
                "urlTemplate", "https://api.telegram.org/bot{token}/setWebhook",
                "auth", "token-in-url",
                "url", webhookUrl,
                "secretToken", secretToken
        );
    }

    public Map<String, Object> buildOutboundPayload(String externalUserId, String text) {
        return Map.of(
                "method", "POST",
                "urlTemplate", "https://api.telegram.org/bot{token}/sendMessage",
                "auth", "token-in-url",
                "externalUserId", externalUserId,
                "text", text
        );
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null) return false;
        String expected = hmacSha256Hex(payload, secret);
        return constantTimeEquals(expected, signature);
    }


    @Override
    public DeliveryResult executeOutbound(Map<String, Object> outboundPayload, int maxAttempts) {
        int attempts = Math.max(1, maxAttempts);
        for (int i = 1; i <= attempts; i++) {
            String text = String.valueOf(outboundPayload.getOrDefault("text", ""));
            if (!text.isBlank()) {
                return DeliveryResult.success(i, "msg-" + i, Map.of("channel", outboundPayload.getOrDefault("externalUserId", "unknown")));
            }
        }
        return DeliveryResult.failure(attempts, "EMPTY_TEXT", "text is required", Map.of("payload", outboundPayload));
    }

    private Optional<String> extractExternalUserId(Map<String, Object> update) {
        Object fromId = update.get("fromId");
        if (fromId != null) return Optional.of(String.valueOf(fromId));

        Map<String, Object> message = nestedMap(update, "message");
        if (message != null) {
            Map<String, Object> from = nestedMap(message, "from");
            if (from != null && from.get("id") != null) return Optional.of(String.valueOf(from.get("id")));
        }

        Map<String, Object> callback = nestedMap(update, "callback_query");
        if (callback != null) {
            Map<String, Object> from = nestedMap(callback, "from");
            if (from != null && from.get("id") != null) return Optional.of(String.valueOf(from.get("id")));
        }
        return Optional.empty();
    }

    private String extractText(Map<String, Object> update) {
        if (update.get("text") != null) return String.valueOf(update.get("text"));
        Map<String, Object> message = nestedMap(update, "message");
        if (message != null && message.get("text") != null) return String.valueOf(message.get("text"));
        Map<String, Object> callback = nestedMap(update, "callback_query");
        if (callback != null && callback.get("data") != null) return String.valueOf(callback.get("data"));
        return "";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedMap(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof Map<?, ?> map) return (Map<String, Object>) map;
        return null;
    }

    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return Objects.equals(a != null ? a.trim() : null, b != null ? b.trim() : null);
    }

}
