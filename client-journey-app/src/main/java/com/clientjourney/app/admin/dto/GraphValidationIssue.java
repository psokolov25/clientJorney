package com.clientjourney.app.admin.dto;

public record GraphValidationIssue(
    String code,
    String nodeId,
    String message
) {
}
