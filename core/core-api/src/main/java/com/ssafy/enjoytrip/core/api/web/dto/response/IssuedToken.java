package com.ssafy.enjoytrip.core.api.web.dto.response;

public record IssuedToken(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
