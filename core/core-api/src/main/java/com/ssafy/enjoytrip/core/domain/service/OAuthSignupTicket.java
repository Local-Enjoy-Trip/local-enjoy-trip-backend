package com.ssafy.enjoytrip.core.domain.service;

public record OAuthSignupTicket(
        String ticket,
        String email,
        String suggestedName,
        long expiresIn
) {
}
