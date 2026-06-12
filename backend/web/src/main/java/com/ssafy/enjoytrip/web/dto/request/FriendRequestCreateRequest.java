package com.ssafy.enjoytrip.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FriendRequestCreateRequest(
        @NotBlank @Size(max = 64) String targetUserId
) {
    public String normalizedTargetUserId() {
        return targetUserId.trim();
    }
}
