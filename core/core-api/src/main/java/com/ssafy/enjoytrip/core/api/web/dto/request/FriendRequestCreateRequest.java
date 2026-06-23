package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FriendRequestCreateRequest(
        @NotBlank @Email @Size(max = 320) String targetEmail
) {
}
