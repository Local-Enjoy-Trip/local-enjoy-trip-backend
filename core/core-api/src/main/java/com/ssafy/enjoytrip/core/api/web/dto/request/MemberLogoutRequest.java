package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberLogoutRequest(@NotBlank String userId) {
}
