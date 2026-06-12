package com.ssafy.enjoytrip.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberLogoutRequest(@NotBlank String userId) {
}
