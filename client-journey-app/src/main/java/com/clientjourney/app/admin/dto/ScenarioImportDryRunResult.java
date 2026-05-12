package com.clientjourney.app.admin.dto;

public record ScenarioImportDryRunResult(
    boolean ok,
    String message,
    String scenarioCode
) {}
