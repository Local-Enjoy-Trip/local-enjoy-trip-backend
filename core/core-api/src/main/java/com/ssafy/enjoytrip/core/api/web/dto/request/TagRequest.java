package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank String name
) {
    public String normalizedName() {
        return name.strip();
    }
}
