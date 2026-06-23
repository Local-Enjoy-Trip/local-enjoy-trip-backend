package com.ssafy.enjoytrip.core.support.auth;

public record IssuedToken(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
