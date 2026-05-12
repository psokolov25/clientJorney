package com.clientjourney.app.admin.dto;

import java.util.List;

public record ScenarioImportValidationResult(
    boolean valid,
    List<String> issues
) {}
