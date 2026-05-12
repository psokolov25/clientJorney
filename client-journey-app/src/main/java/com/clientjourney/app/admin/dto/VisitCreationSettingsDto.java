package com.clientjourney.app.admin.dto;

import java.util.Map;

public record VisitCreationSettingsDto(
    String provider,
    String baseUrl,
    String mode,
    Map<String, String> options
) {
}
