package com.ssafy.enjoytrip.web.dto.response;

public record LoginResponse(
        UserResponse user,
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
