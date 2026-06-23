package com.ssafy.enjoytrip.core.domain.event;

public record FriendshipRequestedEvent(
        Long friendshipId,
        Long requesterMemberId,
        Long recipientMemberId
) {
}
