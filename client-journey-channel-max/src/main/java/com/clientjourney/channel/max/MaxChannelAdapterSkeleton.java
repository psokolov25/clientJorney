package com.clientjourney.channel.max;

import com.clientjourney.channel.spi.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class MaxChannelAdapterSkeleton implements IntegrationReadyChannelAdapter, WebhookSignatureSupport {
    public ClientOutputMessage handle(ClientInputMessage input) { return new ClientOutputMessage(ClientOutputMessageType.INFO, "SKELETON", "MAX skeleton adapter", Map.of("channel", "max")); }
    public List<String> requiredConfigKeys() { return List.of("botToken", "signingSecret", "apiBaseUrl", "timeoutMs"); }
    public boolean supportsWebhookSignatureValidation() { return true; }
    public Map<String, Object> buildOutboundPayload(String externalUserId, String text) { return Map.of("method", "POST", "urlTemplate", "https://api.max.ru/bot/v1/messages", "auth", "Bearer <BOT_TOKEN>", "externalUserId", externalUserId, "text", text); }
    @Override public DeliveryResult executeOutbound(Map<String, Object> outboundPayload, int maxAttempts) { int attempts=Math.max(1,maxAttempts); for(int i=1;i<=attempts;i++){ String text=String.valueOf(outboundPayload.getOrDefault("text","")); if(!text.isBlank()) return DeliveryResult.success(i,"msg-"+i,Map.of("channel",outboundPayload.getOrDefault("externalUserId","unknown"))); } return DeliveryResult.failure(attempts,"EMPTY_TEXT","text is required",Map.of("payload",outboundPayload)); }
    @Override public boolean validateWebhookSignature(String payload, String signature, String secret) { if(payload==null||signature==null||secret==null||secret.isBlank()) return false; String normalized=signature.startsWith("sha256=")?signature.substring("sha256=".length()):signature; String expected=hmacSha256(payload,secret); return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),normalized.getBytes(StandardCharsets.UTF_8)); }
    private String hmacSha256(String payload, String secret) { try { Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256")); return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))); } catch(Exception e){ return ""; } }
}
