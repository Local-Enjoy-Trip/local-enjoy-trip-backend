package com.ssafy.enjoytrip.web.dto.response;

public record OAuthSignupTicket(
        String ticket,
        String email,
        String suggestedName,
        long expiresIn
) {
}
