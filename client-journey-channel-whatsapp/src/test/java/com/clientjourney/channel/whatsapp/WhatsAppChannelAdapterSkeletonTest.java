package com.clientjourney.channel.whatsapp;

import com.clientjourney.channel.spi.ClientOutputMessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WhatsAppChannelAdapterSkeletonTest {
    @Test
    void returns_info() {
        var out = new WhatsAppChannelAdapterSkeleton().handle(null);
        assertEquals(ClientOutputMessageType.INFO, out.type());
    }
}
