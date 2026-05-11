package com.clientjourney.core;

import com.clientjourney.domain.model.ServiceRef;

import java.util.List;

public record ScenarioStepResult(String nodeId, String nodeType, String text, boolean completed, List<ServiceRef> services) {
}
