package com.clientjourney.app.admin.dto;

import java.util.List;

public record ScenarioImportDryRunResult(
    boolean ok,
    String message,
    String scenarioCode,
    int issuesCount,
    List<String> issues
) {}
