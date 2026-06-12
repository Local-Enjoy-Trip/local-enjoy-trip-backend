package com.ssafy.enjoytrip.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NoticeUpdateRequest(
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
