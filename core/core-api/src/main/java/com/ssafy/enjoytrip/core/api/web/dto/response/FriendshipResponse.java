package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Friendship;
import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import java.time.LocalDateTime;

public record FriendshipResponse(
        Long id,
        String requesterEmail,
        String requesterDisplayName,
        String addresseeEmail,
        String addresseeDisplayName,
        FriendshipStatus status,
        LocalDateTime requestedAt,
        LocalDateTime respondedAt
) {
    public static FriendshipResponse from(Friendship friendship) {
        return new FriendshipResponse(
                friendship.id(),
                friendship.requesterEmail(),
                friendship.requesterDisplayName(),
                friendship.addresseeEmail(),
                friendship.addresseeDisplayName(),
                friendship.status(),
                friendship.requestedAt(),
                friendship.respondedAt()
        );
    }
}
