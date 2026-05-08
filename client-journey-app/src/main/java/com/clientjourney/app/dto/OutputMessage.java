package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record OutputMessage(String type, String text) {
}
