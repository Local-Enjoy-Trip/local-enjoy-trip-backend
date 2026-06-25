package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CourseInviteRequest(
        @NotBlank @Email String inviteeEmail
) {
}
