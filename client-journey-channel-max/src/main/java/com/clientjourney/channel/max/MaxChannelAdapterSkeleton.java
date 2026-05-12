package com.clientjourney.channel.max;

import com.clientjourney.channel.spi.ClientInputMessage;
import com.clientjourney.channel.spi.ClientOutputMessage;
import com.clientjourney.channel.spi.ClientOutputMessageType;

import java.util.Map;

public class MaxChannelAdapterSkeleton {
    public ClientOutputMessage handle(ClientInputMessage input) {
        return new ClientOutputMessage(ClientOutputMessageType.INFO, "SKELETON", "MAX skeleton adapter", Map.of("channel", "max"));
    }
}
