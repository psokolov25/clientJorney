package com.clientjourney.app.dto;

import java.util.List;

public record BranchSelectionConfigDto(
    String mode,
    List<String> branches
) {
}
