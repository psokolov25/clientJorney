package com.clientjourney.app.admin.dto;

import com.clientjourney.domain.model.ScenarioGraph;

public record ScenarioImportRequest(
    CreateScenarioRequest scenario,
    ScenarioGraph graph
) {
}
