package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Friendship;
import java.util.List;

public record FriendsResponse(List<FriendResponse> friends) {
    public static FriendsResponse from(List<Friendship> friendships, Long actorMemberId) {
        return new FriendsResponse(friendships.stream()
                .map(friendship -> FriendResponse.from(friendship, actorMemberId))
                .toList());
    }
}
