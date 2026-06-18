package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberOAuthSignupRequest(
        @NotBlank String oauthSignupTicket,
        @NotBlank @Size(min = 2, max = 30) String name
) {
}
