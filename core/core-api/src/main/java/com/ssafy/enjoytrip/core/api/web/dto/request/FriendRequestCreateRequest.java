package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record FriendRequestCreateRequest(
        @NotNull Long targetUserId
) {
}
