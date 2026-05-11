package com.clientjourney.channel.websocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebSocketChannelAdapterSkeletonTest {
    @Test
    void shouldExposeAdapterMetadata() {
        WebSocketChannelAdapterSkeleton adapter = new WebSocketChannelAdapterSkeleton();
        assertEquals("WEBSOCKET", adapter.adapterType());
        assertEquals("NOT_IMPLEMENTED", adapter.status());
    }
}
