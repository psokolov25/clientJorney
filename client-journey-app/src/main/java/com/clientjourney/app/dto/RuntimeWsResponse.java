package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record RuntimeWsResponse(boolean success, StartSessionResponse data, String error) {
    public static RuntimeWsResponse ok(StartSessionResponse data) {
        return new RuntimeWsResponse(true, data, null);
    }

    public static RuntimeWsResponse error(String message) {
        return new RuntimeWsResponse(false, null, message);
    }
}
