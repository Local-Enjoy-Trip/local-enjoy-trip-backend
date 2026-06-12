package com.ssafy.enjoytrip.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NoticeCreateRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank String author
) {
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
