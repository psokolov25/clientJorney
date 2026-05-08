package com.clientjourney.domain.model;

public record AnswerOption(
    String id,
    String code,
    String label,
    String nextNodeId
) {
}
