package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.ACCEPTED;
import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.DELETED;
import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.REJECTED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.FRIENDSHIP_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Friendship;
import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipMapper friendshipMapper;
    private final MemberMapper memberMapper;
    private final NotificationService notificationService;

    @Transactional
    public Friendship requestFriendship(String requesterUserId, String targetUserId) {
        Friendship.validateRequestableUsers(requesterUserId, targetUserId);
        Friendship friendship = savePending(requesterUserId, targetUserId);
        notificationService.saveFriendRequestReceived(friendship.id(), requesterUserId, targetUserId);
        return friendship;
    }

    @Transactional
    public Friendship acceptRequest(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateAcceptableBy(actorUserId);
        Friendship accepted = updateStatus(friendshipId, ACCEPTED);
        markFriendRequestNotificationRead(accepted);
        return accepted;
    }

    @Transactional
    public Friendship rejectRequest(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateRejectableBy(actorUserId);
        Friendship rejected = updateStatus(friendshipId, REJECTED);
        markFriendRequestNotificationRead(rejected);
        return rejected;
    }

    @Transactional
    public Friendship cancelSentRequest(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateCancelableBy(actorUserId);
        return updateStatus(friendshipId, DELETED);
    }

    @Transactional
    public Friendship deleteFriendship(Long friendshipId, String actorUserId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateDeletableBy(actorUserId);
        return updateStatus(friendshipId, DELETED);
    }

    public List<Friendship> findFriends(String actorUserId) {
        return friendshipMapper.findByParticipantAndStatus(actorUserId, ACCEPTED).stream()
                .map(record -> new Friendship(
                        record.getId(),
                        record.getRequesterUserId(),
                        displayName(record.getRequesterUserId()),
                        record.getAddresseeUserId(),
                        displayName(record.getAddresseeUserId()),
                        record.getStatus(),
                        record.getRequestedAt(),
                        record.getRespondedAt(),
                        record.getCreatedAt(),
                        record.getUpdatedAt()
                ))
                .toList();
    }

    public List<Friendship> findReceivedPendingRequests(String actorUserId) {
        return friendshipMapper.findReceivedRequests(actorUserId, PENDING).stream()
                .map(record -> new Friendship(
                        record.getId(),
                        record.getRequesterUserId(),
                        displayName(record.getRequesterUserId()),
                        record.getAddresseeUserId(),
                        displayName(record.getAddresseeUserId()),
                        record.getStatus(),
                        record.getRequestedAt(),
                        record.getRespondedAt(),
                        record.getCreatedAt(),
                        record.getUpdatedAt()
                ))
                .toList();
    }

    public List<Friendship> findSentPendingRequests(String actorUserId) {
        return friendshipMapper.findSentRequests(actorUserId, PENDING).stream()
                .map(record -> new Friendship(
                        record.getId(),
                        record.getRequesterUserId(),
                        displayName(record.getRequesterUserId()),
                        record.getAddresseeUserId(),
                        displayName(record.getAddresseeUserId()),
                        record.getStatus(),
                        record.getRequestedAt(),
                        record.getRespondedAt(),
                        record.getCreatedAt(),
                        record.getUpdatedAt()
                ))
                .toList();
    }

    public Optional<Friendship> findById(Long id) {
        return Optional.ofNullable(friendshipMapper.findById(id))
                .map(record -> new Friendship(
                        record.getId(),
                        record.getRequesterUserId(),
                        displayName(record.getRequesterUserId()),
                        record.getAddresseeUserId(),
                        displayName(record.getAddresseeUserId()),
                        record.getStatus(),
                        record.getRequestedAt(),
                        record.getRespondedAt(),
                        record.getCreatedAt(),
                        record.getUpdatedAt()
                ));
    }

    public boolean existsActiveBetween(String userId, String otherUserId) {
        return friendshipMapper.existsActiveBetween(userId, otherUserId, List.of(PENDING, ACCEPTED)) > 0;
    }

    private Friendship savePending(String requesterUserId, String addresseeUserId) {
        FriendshipRecord record = new FriendshipRecord(requesterUserId, addresseeUserId);
        friendshipMapper.insert(record);
        return new Friendship(
                record.getId(),
                record.getRequesterUserId(),
                record.getRequesterUserId(),
                record.getAddresseeUserId(),
                record.getAddresseeUserId(),
                record.getStatus(),
                record.getRequestedAt(),
                record.getRespondedAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private Friendship updateStatus(Long id, FriendshipStatus status) {
        FriendshipRecord record = friendshipMapper.findById(id);
        if (record == null) {
            throw new IllegalStateException("친구 관계를 찾을 수 없습니다: " + id);
        }
        record.transitionTo(status);
        friendshipMapper.updateStatus(record);
        return new Friendship(
                record.getId(),
                record.getRequesterUserId(),
                displayName(record.getRequesterUserId()),
                record.getAddresseeUserId(),
                displayName(record.getAddresseeUserId()),
                record.getStatus(),
                record.getRequestedAt(),
                record.getRespondedAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private void markFriendRequestNotificationRead(Friendship friendship) {
        notificationService.markReadByReference(
                friendship.addresseeUserId(),
                NotificationReferenceType.FRIENDSHIP,
                friendship.id()
        );
    }

    private Friendship findFriendship(Long friendshipId) {
        FriendshipRecord record = friendshipMapper.findById(friendshipId);
        if (record == null) {
            throw new CoreException(FRIENDSHIP_NOT_FOUND);
        }
        return new Friendship(
                record.getId(),
                record.getRequesterUserId(),
                displayName(record.getRequesterUserId()),
                record.getAddresseeUserId(),
                displayName(record.getAddresseeUserId()),
                record.getStatus(),
                record.getRequestedAt(),
                record.getRespondedAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private String displayName(String userId) {
        MemberRecord member = memberMapper.findByUserId(userId);
        if (member == null) {
            return userId;
        }
        if (member.getNickname() != null && !member.getNickname().isBlank()) {
            return member.getNickname();
        }
        if (member.getName() != null && !member.getName().isBlank()) {
            return member.getName();
        }
        return userId;
    }
}
