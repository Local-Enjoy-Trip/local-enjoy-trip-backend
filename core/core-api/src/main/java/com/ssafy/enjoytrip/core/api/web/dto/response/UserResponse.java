package com.ssafy.enjoytrip.core.api.web.dto.response;

public record UserResponse(
        String name,
        String nickname,
        String email,
        String profileImageUrl
) {
}
