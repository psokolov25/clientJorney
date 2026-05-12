package com.clientjourney.channel.max;

import com.clientjourney.channel.spi.ClientOutputMessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaxChannelAdapterSkeletonTest {
    @Test
    void returns_info() {
        var adapter = new MaxChannelAdapterSkeleton();
        var out = adapter.handle(null);
        assertEquals(ClientOutputMessageType.INFO, out.type());
        assertEquals("max", out.payload().get("channel"));
    }

    @Test
    void exposes_integration_ready_metadata_contract() {
        var adapter = new MaxChannelAdapterSkeleton();
        assertTrue(adapter.supportsWebhookSignatureValidation());
        assertEquals(java.util.List.of("botToken", "signingSecret", "apiBaseUrl", "timeoutMs"), adapter.requiredConfigKeys());
    }

    @Test
    void builds_outbound_payload_contract() {
        var adapter = new MaxChannelAdapterSkeleton();
        var payload = adapter.buildOutboundPayload("user-42", "hello");
        assertEquals("POST", payload.get("method"));
        assertEquals("user-42", payload.get("externalUserId"));
        assertEquals("hello", payload.get("text"));
        assertNotNull(payload.get("urlTemplate"));
        assertNotNull(payload.get("auth"));
    }

    @Test
    void validates_webhook_signature() {
        var adapter = new MaxChannelAdapterSkeleton();
        String payload = "{\"event\":\"message\"}";
        String secret = "super-secret";
        String signature = "sha256=3768adfa09765f3309b1b5c7db4fbf8a8969470a3a450d501f1c0e9b8c230fef";

        assertTrue(adapter.validateWebhookSignature(payload, signature, secret));
        assertFalse(adapter.validateWebhookSignature(payload, "sha256=deadbeef", secret));
    }


    @Test
    void reports_delivery_statuses_and_errors() {
        var adapter = new MaxChannelAdapterSkeleton();
        var ok = adapter.executeOutbound(java.util.Map.of("externalUserId", "user-1", "text", "hello"), 2);
        assertTrue(ok.delivered());
        assertEquals(1, ok.attempts());

        var fail = adapter.executeOutbound(java.util.Map.of("externalUserId", "user-1", "text", ""), 2);
        assertFalse(fail.delivered());
        assertEquals("EMPTY_TEXT", fail.errorCode());
        assertEquals(2, fail.attempts());
    }
}
