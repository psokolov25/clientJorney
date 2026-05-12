package com.clientjourney.channel.spi;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class WebhookSignatures {
    private WebhookSignatures() {
    }

    public static boolean validateSha256Hmac(String payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null || secret.isBlank()) {
            return false;
        }
        String normalized = signature.startsWith("sha256=")
                ? signature.substring("sha256=".length())
                : signature;
        String expected = hmacSha256(payload, secret);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), normalized.getBytes(StandardCharsets.UTF_8));
    }

    public static String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return "";
        }
    }
}
