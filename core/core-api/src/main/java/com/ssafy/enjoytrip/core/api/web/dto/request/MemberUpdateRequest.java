package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @Size(min = 2, max = 30)
        String nickname,

        @Size(max = 512)
        String profileImageUrl
) {
    public String normalizedNickname() {
        return trimToNull(nickname);
    }

    public String normalizedProfileImageUrl() {
        return trimToNull(profileImageUrl);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
