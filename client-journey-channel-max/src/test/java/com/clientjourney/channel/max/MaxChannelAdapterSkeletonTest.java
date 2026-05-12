package com.clientjourney.channel.max;

import com.clientjourney.channel.spi.ClientOutputMessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaxChannelAdapterSkeletonTest {
    @Test
    void returns_info() {
        var out = new MaxChannelAdapterSkeleton().handle(null);
        assertEquals(ClientOutputMessageType.INFO, out.type());
    }
}
