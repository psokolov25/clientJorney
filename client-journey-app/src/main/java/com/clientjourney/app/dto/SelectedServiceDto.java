package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record SelectedServiceDto(String serviceId, String serviceCode, String serviceName) {
}
