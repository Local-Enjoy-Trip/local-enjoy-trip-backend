package com.ssafy.enjoytrip.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank String name
) {
    public String normalizedName() {
        return name.strip();
    }
}
