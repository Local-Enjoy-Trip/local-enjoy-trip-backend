package com.ssafy.enjoytrip.core.domain.service;

public record IssuedToken(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
