package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.ACCEPTED;
import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.FRIENDSHIP_ACCESS_DENIED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.FRIENDSHIP_INVALID_STATE;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.FRIENDSHIP_SELF_REQUEST;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import java.time.LocalDateTime;

public record Friendship(
        Long id,
        Long requesterMemberId,
        String requesterEmail,
        String requesterDisplayName,
        Long addresseeMemberId,
        String addresseeEmail,
        String addresseeDisplayName,
        FriendshipStatus status,
        LocalDateTime requestedAt,
        LocalDateTime respondedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static void validateRequestableMembers(Long requesterMemberId, Long targetMemberId) {
        if (requesterMemberId.equals(targetMemberId)) {
            throw new CoreException(FRIENDSHIP_SELF_REQUEST);
        }
    }

    public void validateAcceptableBy(Long actorMemberId) {
        validatePending();
        validateAddressee(actorMemberId);
    }

    public void validateRejectableBy(Long actorMemberId) {
        validatePending();
        validateAddressee(actorMemberId);
    }

    public void validateCancelableBy(Long actorMemberId) {
        validatePending();
        validateRequester(actorMemberId);
    }

    public void validateDeletableBy(Long actorMemberId) {
        validateAccepted();
        validateParticipant(actorMemberId);
    }

    public String counterpartEmail(Long actorMemberId) {
        if (isRequester(actorMemberId)) {
            return addresseeEmail;
        }
        if (isAddressee(actorMemberId)) {
            return requesterEmail;
        }
        return null;
    }

    public String counterpartDisplayName(Long actorMemberId) {
        if (isRequester(actorMemberId)) {
            return addresseeDisplayName;
        }
        if (isAddressee(actorMemberId)) {
            return requesterDisplayName;
        }
        return null;
    }

    public boolean isParticipant(Long actorMemberId) {
        return isRequester(actorMemberId) || isAddressee(actorMemberId);
    }

    public boolean isRequester(Long actorMemberId) {
        return requesterMemberId.equals(actorMemberId);
    }

    public boolean isAddressee(Long actorMemberId) {
        return addresseeMemberId.equals(actorMemberId);
    }

    public boolean isPending() {
        return status == PENDING;
    }

    public boolean isAccepted() {
        return status == ACCEPTED;
    }

    private void validatePending() {
        if (!isPending()) {
            throw new CoreException(FRIENDSHIP_INVALID_STATE);
        }
    }

    private void validateAccepted() {
        if (!isAccepted()) {
            throw new CoreException(FRIENDSHIP_INVALID_STATE);
        }
    }

    private void validateAddressee(Long actorMemberId) {
        if (!isAddressee(actorMemberId)) {
            throw new CoreException(FRIENDSHIP_ACCESS_DENIED);
        }
    }

    private void validateRequester(Long actorMemberId) {
        if (!isRequester(actorMemberId)) {
            throw new CoreException(FRIENDSHIP_ACCESS_DENIED);
        }
    }

    private void validateParticipant(Long actorMemberId) {
        if (!isParticipant(actorMemberId)) {
            throw new CoreException(FRIENDSHIP_ACCESS_DENIED);
        }
    }
}
