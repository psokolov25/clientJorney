package com.clientjourney.channel.websocket;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Requires(property = "client-journey.channel.websocket.enabled", value = "true")
@Singleton
public class WebSocketChannelAdapterSkeleton {

    public String adapterType() {
        return "WEBSOCKET";
    }

    public String status() {
        return "NOT_IMPLEMENTED";
    }
}
