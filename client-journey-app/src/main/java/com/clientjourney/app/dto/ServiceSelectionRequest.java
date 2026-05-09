package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
public record ServiceSelectionRequest(List<SelectedServiceDto> selectedServices) {
}
