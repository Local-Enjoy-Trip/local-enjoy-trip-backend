package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Friendship;
import java.util.List;

public record FriendshipRequestsResponse(List<FriendshipResponse> requests) {
    public static FriendshipRequestsResponse from(List<Friendship> friendships) {
        return new FriendshipRequestsResponse(friendships.stream()
                .map(FriendshipResponse::from)
                .toList());
    }
}
