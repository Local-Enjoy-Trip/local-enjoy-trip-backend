package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Friendship;

public record FriendResponse(
        Long friendshipId,
        String email,
        String displayName
) {
    public static FriendResponse from(Friendship friendship, Long actorMemberId) {
        return new FriendResponse(
                friendship.id(),
                friendship.counterpartEmail(actorMemberId),
                friendship.counterpartDisplayName(actorMemberId)
        );
    }
}
