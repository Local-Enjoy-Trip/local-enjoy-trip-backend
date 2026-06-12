package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Friendship;
import com.ssafy.enjoytrip.domain.FriendshipStatus;
import java.time.LocalDateTime;

public record FriendshipResponse(
        Long id,
        String requesterUserId,
        String requesterDisplayName,
        String addresseeUserId,
        String addresseeDisplayName,
        FriendshipStatus status,
        LocalDateTime requestedAt,
        LocalDateTime respondedAt
) {
    public static FriendshipResponse from(Friendship friendship) {
        return new FriendshipResponse(
                friendship.id(),
                friendship.requesterUserId(),
                friendship.requesterDisplayName(),
                friendship.addresseeUserId(),
                friendship.addresseeDisplayName(),
                friendship.status(),
                friendship.requestedAt(),
                friendship.respondedAt()
        );
    }
}
