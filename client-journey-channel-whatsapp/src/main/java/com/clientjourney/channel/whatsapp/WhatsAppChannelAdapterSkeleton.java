package com.clientjourney.channel.whatsapp;

import com.clientjourney.channel.spi.*;

import java.util.Map;

public class WhatsAppChannelAdapterSkeleton implements IntegrationReadyChannelAdapter, WebhookSignatureSupport {
    public ClientOutputMessage handle(ClientInputMessage input) {
        return new ClientOutputMessage(ClientOutputMessageType.INFO, "SKELETON", "WhatsApp skeleton adapter", Map.of("channel", "whatsapp"));
    }

    public java.util.List<String> requiredConfigKeys() {
        return java.util.List.of("webhookToken", "accessToken", "webhookSecret", "phoneNumberId", "apiBaseUrl", "timeoutMs");
    }

    public boolean supportsWebhookSignatureValidation() { return true; }

    public Map<String, Object> buildOutboundPayload(String externalUserId, String text) {
        return Map.of("method", "POST", "urlTemplate", "https://graph.facebook.com/v19.0/{phoneNumberId}/messages", "auth", "Bearer <ACCESS_TOKEN>", "externalUserId", externalUserId, "text", text);
    }

    @Override
    public DeliveryResult executeOutbound(Map<String, Object> outboundPayload, int maxAttempts) {
        int attempts = Math.max(1, maxAttempts);
        for (int i = 1; i <= attempts; i++) {
            String text = String.valueOf(outboundPayload.getOrDefault("text", ""));
            if (!text.isBlank()) return DeliveryResult.success(i, "msg-" + i, Map.of("channel", outboundPayload.getOrDefault("externalUserId", "unknown")));
        }
        return DeliveryResult.failure(attempts, "EMPTY_TEXT", "text is required", Map.of("payload", outboundPayload));
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature, String secret) {
        return WebhookSignatures.validateSha256Hmac(payload, signature, secret);
    }
}
