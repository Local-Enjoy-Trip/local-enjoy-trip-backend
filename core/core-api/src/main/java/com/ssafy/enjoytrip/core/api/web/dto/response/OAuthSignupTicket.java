package com.ssafy.enjoytrip.core.api.web.dto.response;

public record OAuthSignupTicket(
        String ticket,
        String email,
        String suggestedName,
        long expiresIn
) {
}
