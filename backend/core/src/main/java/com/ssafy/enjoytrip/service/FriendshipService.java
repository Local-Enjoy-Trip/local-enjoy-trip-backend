package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.domain.FriendshipStatus.ACCEPTED;
import static com.ssafy.enjoytrip.domain.FriendshipStatus.DELETED;
import static com.ssafy.enjoytrip.domain.FriendshipStatus.REJECTED;
import static com.ssafy.enjoytrip.support.error.ErrorType.FRIENDSHIP_NOT_FOUND;

import com.ssafy.enjoytrip.domain.Friendship;
import com.ssafy.enjoytrip.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.repository.FriendshipRepository;
import com.ssafy.enjoytrip.repository.NotificationOutboxRepository;
import com.ssafy.enjoytrip.repository.NotificationRepository;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public Friendship requestFriendship(String requesterUserId, String targetUserId) {
        Friendship.validateRequestableUsers(requesterUserId, targetUserId);
        Friendship friendship = friendshipRepository.savePending(requesterUserId, targetUserId);
        notificationOutboxRepository.saveFriendRequestReceived(
                friendship.id(),
                requesterUserId,
                targetUserId
        );
        return friendship;
    }

    @Transactional
    public Friendship acceptRequest(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateAcceptableBy(actorUserId);
        Friendship accepted = friendshipRepository.updateStatus(friendshipId, ACCEPTED);
        markFriendRequestNotificationRead(accepted);
        return accepted;
    }

    @Transactional
    public Friendship rejectRequest(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateRejectableBy(actorUserId);
        Friendship rejected = friendshipRepository.updateStatus(friendshipId, REJECTED);
        markFriendRequestNotificationRead(rejected);
        return rejected;
    }

    @Transactional
    public Friendship cancelSentRequest(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateCancelableBy(actorUserId);
        return friendshipRepository.updateStatus(friendshipId, DELETED);
    }

    @Transactional
    public Friendship deleteFriendship(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateDeletableBy(actorUserId);
        return friendshipRepository.updateStatus(friendshipId, DELETED);
    }

    @Transactional(readOnly = true)
    public List<Friendship> findFriends(String actorUserId) {
        return friendshipRepository.findAcceptedByUser(actorUserId);
    }

    @Transactional(readOnly = true)
    public List<Friendship> findReceivedPendingRequests(String actorUserId) {
        return friendshipRepository.findPendingReceivedByUser(actorUserId);
    }

    @Transactional(readOnly = true)
    public List<Friendship> findSentPendingRequests(String actorUserId) {
        return friendshipRepository.findPendingSentByUser(actorUserId);
    }

    private void markFriendRequestNotificationRead(Friendship friendship) {
        notificationRepository.markReadByReference(
                friendship.addresseeUserId(),
                NotificationReferenceType.FRIENDSHIP,
                friendship.id()
        );
    }

    private Friendship findFriendship(Long friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new CoreException(FRIENDSHIP_NOT_FOUND));
    }
}
