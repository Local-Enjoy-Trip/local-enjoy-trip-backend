package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.domain.FriendshipStatus.ACCEPTED;
import static com.ssafy.enjoytrip.domain.FriendshipStatus.DELETED;
import static com.ssafy.enjoytrip.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.domain.FriendshipStatus.REJECTED;
import static com.ssafy.enjoytrip.support.error.ErrorType.FRIENDSHIP_ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.FRIENDSHIP_ALREADY_ACTIVE;
import static com.ssafy.enjoytrip.support.error.ErrorType.FRIENDSHIP_INVALID_STATE;
import static com.ssafy.enjoytrip.support.error.ErrorType.FRIENDSHIP_NOT_FOUND;
import static com.ssafy.enjoytrip.support.error.ErrorType.FRIENDSHIP_SELF_REQUEST;
import static com.ssafy.enjoytrip.support.error.ErrorType.USER_NOT_FOUND;

import com.ssafy.enjoytrip.domain.Friendship;
import com.ssafy.enjoytrip.domain.FriendshipStatus;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.repository.FriendshipRepository;
import com.ssafy.enjoytrip.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    @Transactional
    public Friendship requestFriendship(String requesterUserId, String targetUserId) {
        validateDifferentUsers(requesterUserId, targetUserId);
        Member requester = findExistingMember(requesterUserId);
        findExistingMember(targetUserId);
        validateNoActiveRelationship(requesterUserId, targetUserId);

        Friendship friendship = friendshipRepository.savePending(requesterUserId, targetUserId);
        notificationOutboxRepository.saveFriendRequestReceived(
                friendship.id(),
                requesterUserId,
                displayName(requester),
                targetUserId
        );
        return friendship;
    }

    @Transactional
    public Friendship acceptRequest(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        validatePending(friendship);
        validateAddressee(friendship, actorUserId);
        Friendship accepted = friendshipRepository.updateStatus(friendshipId, ACCEPTED);
        markFriendRequestNotificationRead(accepted);
        return accepted;
    }

    @Transactional
    public Friendship rejectRequest(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        validatePending(friendship);
        validateAddressee(friendship, actorUserId);
        Friendship rejected = friendshipRepository.updateStatus(friendshipId, REJECTED);
        markFriendRequestNotificationRead(rejected);
        return rejected;
    }

    @Transactional
    public Friendship cancelSentRequest(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        validatePending(friendship);
        validateRequester(friendship, actorUserId);
        return friendshipRepository.updateStatus(friendshipId, DELETED);
    }

    @Transactional
    public Friendship deleteFriendship(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        validateAccepted(friendship);
        validateParticipant(friendship, actorUserId);
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

    private void validateDifferentUsers(String requesterUserId, String targetUserId) {
        if (requesterUserId.equals(targetUserId)) {
            throw new CoreException(FRIENDSHIP_SELF_REQUEST);
        }
    }

    private Member findExistingMember(String userId) {
        if (!memberRepository.existsByUserId(userId)) {
            throw new CoreException(USER_NOT_FOUND);
        }
        return memberRepository.findByUserId(userId);
    }

    private void validateNoActiveRelationship(String requesterUserId, String targetUserId) {
        if (friendshipRepository.existsActiveBetween(requesterUserId, targetUserId)) {
            throw new CoreException(FRIENDSHIP_ALREADY_ACTIVE);
        }
    }

    private Friendship findFriendship(Long friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new CoreException(FRIENDSHIP_NOT_FOUND));
    }

    private void validatePending(Friendship friendship) {
        validateStatus(friendship, PENDING);
    }

    private void validateAccepted(Friendship friendship) {
        validateStatus(friendship, ACCEPTED);
    }

    private void validateStatus(Friendship friendship, FriendshipStatus expected) {
        if (friendship.status() != expected) {
            throw new CoreException(FRIENDSHIP_INVALID_STATE);
        }
    }

    private void validateAddressee(Friendship friendship, String actorUserId) {
        if (!friendship.addresseeUserId().equals(actorUserId)) {
            throw new CoreException(FRIENDSHIP_ACCESS_DENIED);
        }
    }

    private void validateRequester(Friendship friendship, String actorUserId) {
        if (!friendship.requesterUserId().equals(actorUserId)) {
            throw new CoreException(FRIENDSHIP_ACCESS_DENIED);
        }
    }

    private void validateParticipant(Friendship friendship, String actorUserId) {
        if (!friendship.isParticipant(actorUserId)) {
            throw new CoreException(FRIENDSHIP_ACCESS_DENIED);
        }
    }

    private static String displayName(Member member) {
        if (member.nickname() != null && !member.nickname().isBlank()) {
            return member.nickname();
        }
        if (member.name() != null && !member.name().isBlank()) {
            return member.name();
        }
        return member.userId();
    }
}
