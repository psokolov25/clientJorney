package com.clientjourney.domain.model;

import java.util.List;

public record ScenarioNode(
    String id,
    NodeType type,
    String code,
    String text,
    List<AnswerOption> answers,
    List<ServiceRef> services
) {
}
