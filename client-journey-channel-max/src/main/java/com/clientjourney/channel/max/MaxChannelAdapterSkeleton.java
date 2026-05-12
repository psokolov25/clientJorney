package com.clientjourney.channel.max;

import com.clientjourney.channel.spi.*;
import java.util.List;
import java.util.Map;

public class MaxChannelAdapterSkeleton implements IntegrationReadyChannelAdapter, WebhookSignatureSupport {
    public ClientOutputMessage handle(ClientInputMessage input) { return new ClientOutputMessage(ClientOutputMessageType.INFO, "SKELETON", "MAX skeleton adapter", Map.of("channel", "max")); }
    public List<String> requiredConfigKeys() { return List.of("botToken", "signingSecret", "apiBaseUrl", "timeoutMs"); }
    public boolean supportsWebhookSignatureValidation() { return true; }
    public Map<String, Object> buildOutboundPayload(String externalUserId, String text) { return Map.of("method", "POST", "urlTemplate", "https://api.max.ru/bot/v1/messages", "auth", "Bearer <BOT_TOKEN>", "externalUserId", externalUserId, "text", text); }
    @Override public DeliveryResult executeOutbound(Map<String, Object> outboundPayload, int maxAttempts) { int attempts=Math.max(1,maxAttempts); for(int i=1;i<=attempts;i++){ String text=String.valueOf(outboundPayload.getOrDefault("text","")); if(!text.isBlank()) return DeliveryResult.success(i,"msg-"+i,Map.of("channel",outboundPayload.getOrDefault("externalUserId","unknown"))); } return DeliveryResult.failure(attempts,"EMPTY_TEXT","text is required",Map.of("payload",outboundPayload)); }
    @Override public boolean validateWebhookSignature(String payload, String signature, String secret) { return WebhookSignatures.validateSha256Hmac(payload, signature, secret); }
}
