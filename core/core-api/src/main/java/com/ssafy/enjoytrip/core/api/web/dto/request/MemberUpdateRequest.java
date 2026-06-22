package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @Size(min = 2, max = 30)
        String nickname
) {
    public String normalizedNickname() {
        return trimToNull(nickname);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
