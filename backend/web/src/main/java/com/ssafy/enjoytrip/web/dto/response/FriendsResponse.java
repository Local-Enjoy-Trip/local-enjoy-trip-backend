package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Friendship;
import java.util.List;

public record FriendsResponse(List<FriendResponse> friends) {
    public static FriendsResponse from(List<Friendship> friendships, String actorUserId) {
        return new FriendsResponse(friendships.stream()
                .map(friendship -> FriendResponse.from(friendship, actorUserId))
                .toList());
    }
}
