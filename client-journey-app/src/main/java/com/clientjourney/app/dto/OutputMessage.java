package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

import java.util.Map;

@Introspected
public record OutputMessage(String type, String text, Map<String, Object> payload) {
    public static OutputMessage info(String text) {
        return new OutputMessage("INFO", text, Map.of());
    }

    public static OutputMessage result(String text) {
        return new OutputMessage("RESULT", text, Map.of());
    }

    public static OutputMessage serviceSelection(String text, int minSelectedServices, Integer maxSelectedServices, Object services) {
        return new OutputMessage(
            "SERVICE_SELECTION",
            text,
            Map.of(
                "minSelectedServices", minSelectedServices,
                "maxSelectedServices", maxSelectedServices,
                "services", services
            )
        );
    }
}
