package com.ssafy.enjoytrip.web.dto.response;

public record IssuedToken(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
