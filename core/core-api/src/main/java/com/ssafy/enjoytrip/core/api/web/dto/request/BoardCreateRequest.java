package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BoardCreateRequest(
        @NotBlank String id,
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank String author
) {
    public String normalizedId() {
        return id.strip();
    }

    public String normalizedTitle() {
        return title.strip();
    }

    public String normalizedContent() {
        return content.strip();
    }

    public String normalizedAuthor() {
        return author.strip();
    }
}
