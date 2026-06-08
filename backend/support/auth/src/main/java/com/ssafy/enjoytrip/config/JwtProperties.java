package com.ssafy.enjoytrip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "enjoytrip.jwt")
public record JwtProperties(
        String secret,
        long expirationSeconds
) {
    private static final String DEFAULT_SECRET = "dev-only-enjoytrip-jwt-secret-change-me-please-32b";

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            secret = DEFAULT_SECRET;
        }
        if (expirationSeconds <= 0) {
            expirationSeconds = 7200;
        }
    }
}
