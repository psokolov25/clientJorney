package com.clientjourney.channel.websocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebSocketChannelAdapterSkeletonTest {
    @Test
    void shouldExposeAdapterMetadata() {
        WebSocketChannelAdapterSkeleton adapter = new WebSocketChannelAdapterSkeleton();
        assertEquals("WEBSOCKET", adapter.adapterType());
        assertEquals("NOT_IMPLEMENTED", adapter.status());
        assertEquals("ws.v1", adapter.contractVersion());
        assertTrue(adapter.supportedInboundEventTypes().contains("PING"));
        assertTrue(adapter.supportedOutboundEventTypes().contains("PONG"));
    }

    @Test
    void shouldBuildDeliveryAckPayloadWithContractVersion() {
        WebSocketChannelAdapterSkeleton adapter = new WebSocketChannelAdapterSkeleton();
        var ack = adapter.buildDeliveryAck("msg-1", "corr-1", true);
        assertEquals("DELIVERY_STATUS", ack.get("eventType"));
        assertEquals("ws.v1", ack.get("contractVersion"));
        assertEquals("msg-1", ack.get("messageId"));
        assertEquals("corr-1", ack.get("correlationId"));
        assertEquals("sent", ack.get("status"));
    }
}
