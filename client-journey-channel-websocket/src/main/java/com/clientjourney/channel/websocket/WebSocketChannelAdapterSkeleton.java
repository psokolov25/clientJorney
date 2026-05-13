package com.clientjourney.channel.websocket;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

@Requires(property = "client-journey.channel.websocket.enabled", value = "true")
@Singleton
public class WebSocketChannelAdapterSkeleton {

    public String adapterType() {
        return "WEBSOCKET";
    }

    public String status() {
        return "NOT_IMPLEMENTED";
    }

    public String contractVersion() {
        return "ws.v1";
    }

    public List<String> supportedInboundEventTypes() {
        return List.of("SESSION_INIT", "USER_MESSAGE", "ACK", "PING");
    }

    public List<String> supportedOutboundEventTypes() {
        return List.of("BOT_MESSAGE", "DELIVERY_STATUS", "ERROR", "PONG");
    }

    public Map<String, Object> buildDeliveryAck(String messageId, String correlationId, boolean delivered) {
        return Map.of(
                "eventType", "DELIVERY_STATUS",
                "contractVersion", contractVersion(),
                "messageId", messageId == null ? "" : messageId,
                "correlationId", correlationId == null ? "" : correlationId,
                "status", delivered ? "sent" : "failed"
        );
    }
}
