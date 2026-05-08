package com.clientjourney.app.admin.dto;

import java.util.List;

public record GraphValidationResult(
    boolean valid,
    List<GraphValidationIssue> errors,
    List<GraphValidationIssue> warnings
) {
}
