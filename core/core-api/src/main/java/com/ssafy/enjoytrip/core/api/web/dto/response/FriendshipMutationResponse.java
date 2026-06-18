package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Friendship;

public record FriendshipMutationResponse(FriendshipResponse friendship) {
    public static FriendshipMutationResponse from(Friendship friendship) {
        return new FriendshipMutationResponse(FriendshipResponse.from(friendship));
    }
}
