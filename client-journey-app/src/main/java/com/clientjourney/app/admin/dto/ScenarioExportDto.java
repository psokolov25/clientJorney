package com.clientjourney.app.admin.dto;

import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioGraph;

public record ScenarioExportDto(
    Scenario scenario,
    ScenarioGraph graph
) {
}
