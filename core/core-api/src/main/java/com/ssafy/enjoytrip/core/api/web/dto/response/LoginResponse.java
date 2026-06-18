package com.ssafy.enjoytrip.core.api.web.dto.response;

public record LoginResponse(
        UserResponse user,
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
