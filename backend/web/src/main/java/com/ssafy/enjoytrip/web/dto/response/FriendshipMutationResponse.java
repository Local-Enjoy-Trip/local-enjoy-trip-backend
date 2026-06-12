package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Friendship;

public record FriendshipMutationResponse(FriendshipResponse friendship) {
    public static FriendshipMutationResponse from(Friendship friendship) {
        return new FriendshipMutationResponse(FriendshipResponse.from(friendship));
    }
}
