package com.clientjourney.channel.facebook;

import com.clientjourney.channel.spi.ClientOutputMessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FacebookChannelAdapterSkeletonTest {
    @Test
    void returns_info() {
        var out = new FacebookChannelAdapterSkeleton().handle(null);
        assertEquals(ClientOutputMessageType.INFO, out.type());
    }
}
