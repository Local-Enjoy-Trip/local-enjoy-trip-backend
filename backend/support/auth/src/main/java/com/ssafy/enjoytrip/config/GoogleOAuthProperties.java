package com.ssafy.enjoytrip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "enjoytrip.oauth2.google")
public record GoogleOAuthProperties(
        String clientId,
        String clientSecret
) {
    public boolean enabled() {
        return hasText(clientId) && hasText(clientSecret);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
