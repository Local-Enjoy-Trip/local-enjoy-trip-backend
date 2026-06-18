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
        String requesterUserId,
        String requesterDisplayName,
        String addresseeUserId,
        String addresseeDisplayName,
        FriendshipStatus status,
        LocalDateTime requestedAt,
        LocalDateTime respondedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static void validateRequestableUsers(String requesterUserId, String targetUserId) {
        if (requesterUserId.equals(targetUserId)) {
            throw new CoreException(FRIENDSHIP_SELF_REQUEST);
        }
    }

    public void validateAcceptableBy(String actorUserId) {
        validatePending();
        validateAddressee(actorUserId);
    }

    public void validateRejectableBy(String actorUserId) {
        validatePending();
        validateAddressee(actorUserId);
    }

    public void validateCancelableBy(String actorUserId) {
        validatePending();
        validateRequester(actorUserId);
    }

    public void validateDeletableBy(String actorUserId) {
        validateAccepted();
        validateParticipant(actorUserId);
    }

    public String counterpartUserId(String actorUserId) {
        if (isRequester(actorUserId)) {
            return addresseeUserId;
        }
        if (isAddressee(actorUserId)) {
            return requesterUserId;
        }
        return null;
    }

    public String counterpartDisplayName(String actorUserId) {
        if (isRequester(actorUserId)) {
            return addresseeDisplayName;
        }
        if (isAddressee(actorUserId)) {
            return requesterDisplayName;
        }
        return null;
    }

    public boolean isParticipant(String actorUserId) {
        return isRequester(actorUserId) || isAddressee(actorUserId);
    }

    public boolean isRequester(String actorUserId) {
        return requesterUserId.equals(actorUserId);
    }

    public boolean isAddressee(String actorUserId) {
        return addresseeUserId.equals(actorUserId);
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

    private void validateAddressee(String actorUserId) {
        if (!isAddressee(actorUserId)) {
            throw new CoreException(FRIENDSHIP_ACCESS_DENIED);
        }
    }

    private void validateRequester(String actorUserId) {
        if (!isRequester(actorUserId)) {
            throw new CoreException(FRIENDSHIP_ACCESS_DENIED);
        }
    }

    private void validateParticipant(String actorUserId) {
        if (!isParticipant(actorUserId)) {
            throw new CoreException(FRIENDSHIP_ACCESS_DENIED);
        }
    }
}
