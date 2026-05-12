package com.clientjourney.channel.telegram;

import com.clientjourney.channel.spi.ClientInputMessage;
import com.clientjourney.channel.spi.IntegrationReadyChannelAdapter;
import com.clientjourney.channel.spi.WebhookSignatureSupport;
import com.clientjourney.channel.spi.DeliveryResult;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Requires(property = "client-journey.channel.telegram.enabled", value = "true")
@Singleton
public class TelegramPollingAdapterSkeleton implements IntegrationReadyChannelAdapter, WebhookSignatureSupport {

    public String adapterType() {
        return "TELEGRAM_POLLING";
    }

    public String status() {
        return "NOT_IMPLEMENTED";
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
        String externalUserId = String.valueOf(update.getOrDefault("fromId", "unknown"));
        String text = String.valueOf(update.getOrDefault("text", ""));
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
        String expected = Integer.toHexString((payload + secret).hashCode());
        return expected.equals(signature);
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

}
