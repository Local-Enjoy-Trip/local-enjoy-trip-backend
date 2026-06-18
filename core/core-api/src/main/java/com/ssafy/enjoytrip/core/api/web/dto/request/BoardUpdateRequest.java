package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BoardUpdateRequest(
        @NotBlank String title,
        @NotBlank String content
) {
    public String normalizedTitle() {
        return title.strip();
    }

    public String normalizedContent() {
        return content.strip();
    }
}
