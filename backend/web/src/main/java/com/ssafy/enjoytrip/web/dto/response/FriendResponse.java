package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Friendship;

public record FriendResponse(
        Long friendshipId,
        String userId,
        String displayName
) {
    public static FriendResponse from(Friendship friendship, String actorUserId) {
        return new FriendResponse(
                friendship.id(),
                friendship.counterpartUserId(actorUserId),
                friendship.counterpartDisplayName(actorUserId)
        );
    }
}
