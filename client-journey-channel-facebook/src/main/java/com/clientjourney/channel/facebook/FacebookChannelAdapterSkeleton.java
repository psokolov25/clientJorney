package com.clientjourney.channel.facebook;

import com.clientjourney.channel.spi.ClientInputMessage;
import com.clientjourney.channel.spi.ClientOutputMessage;
import com.clientjourney.channel.spi.ClientOutputMessageType;

import java.util.Map;

public class FacebookChannelAdapterSkeleton {
    public ClientOutputMessage handle(ClientInputMessage input) {
        return new ClientOutputMessage(ClientOutputMessageType.INFO, "SKELETON", "Facebook skeleton adapter", Map.of("channel", "facebook"));
    }
}
