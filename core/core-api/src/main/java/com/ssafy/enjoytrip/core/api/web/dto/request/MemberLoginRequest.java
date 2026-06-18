package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequest(
        @NotBlank String userId,
        @NotBlank String password
) {
}
