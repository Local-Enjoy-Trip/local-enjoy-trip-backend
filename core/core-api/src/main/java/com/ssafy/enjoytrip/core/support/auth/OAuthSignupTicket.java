package com.ssafy.enjoytrip.core.support.auth;

public record OAuthSignupTicket(
        String ticket,
        String email,
        String suggestedName,
        long expiresIn
) {
}
