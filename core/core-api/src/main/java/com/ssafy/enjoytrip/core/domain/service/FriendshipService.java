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
import com.ssafy.enjoytrip.storage.db.core.entity.FriendshipEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.MemberEntity;
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
                .map(entity -> new Friendship(
                        entity.getId(),
                        entity.getRequesterUserId(),
                        displayName(entity.getRequesterUserId()),
                        entity.getAddresseeUserId(),
                        displayName(entity.getAddresseeUserId()),
                        entity.getStatus(),
                        entity.getRequestedAt(),
                        entity.getRespondedAt(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()
                ))
                .toList();
    }

    public List<Friendship> findReceivedPendingRequests(String actorUserId) {
        return friendshipMapper.findReceivedRequests(actorUserId, PENDING).stream()
                .map(entity -> new Friendship(
                        entity.getId(),
                        entity.getRequesterUserId(),
                        displayName(entity.getRequesterUserId()),
                        entity.getAddresseeUserId(),
                        displayName(entity.getAddresseeUserId()),
                        entity.getStatus(),
                        entity.getRequestedAt(),
                        entity.getRespondedAt(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()
                ))
                .toList();
    }

    public List<Friendship> findSentPendingRequests(String actorUserId) {
        return friendshipMapper.findSentRequests(actorUserId, PENDING).stream()
                .map(entity -> new Friendship(
                        entity.getId(),
                        entity.getRequesterUserId(),
                        displayName(entity.getRequesterUserId()),
                        entity.getAddresseeUserId(),
                        displayName(entity.getAddresseeUserId()),
                        entity.getStatus(),
                        entity.getRequestedAt(),
                        entity.getRespondedAt(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()
                ))
                .toList();
    }

    public Optional<Friendship> findById(Long id) {
        return Optional.ofNullable(friendshipMapper.findById(id))
                .map(entity -> new Friendship(
                        entity.getId(),
                        entity.getRequesterUserId(),
                        displayName(entity.getRequesterUserId()),
                        entity.getAddresseeUserId(),
                        displayName(entity.getAddresseeUserId()),
                        entity.getStatus(),
                        entity.getRequestedAt(),
                        entity.getRespondedAt(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()
                ));
    }

    public boolean existsActiveBetween(String userId, String otherUserId) {
        return friendshipMapper.existsActiveBetween(userId, otherUserId, List.of(PENDING, ACCEPTED)) > 0;
    }

    private Friendship savePending(String requesterUserId, String addresseeUserId) {
        FriendshipEntity entity = new FriendshipEntity(requesterUserId, addresseeUserId);
        friendshipMapper.insert(entity);
        return new Friendship(
                entity.getId(),
                entity.getRequesterUserId(),
                entity.getRequesterUserId(),
                entity.getAddresseeUserId(),
                entity.getAddresseeUserId(),
                entity.getStatus(),
                entity.getRequestedAt(),
                entity.getRespondedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private Friendship updateStatus(Long id, FriendshipStatus status) {
        FriendshipEntity entity = friendshipMapper.findById(id);
        if (entity == null) {
            throw new IllegalStateException("친구 관계를 찾을 수 없습니다: " + id);
        }
        entity.transitionTo(status);
        friendshipMapper.updateStatus(entity);
        return new Friendship(
                entity.getId(),
                entity.getRequesterUserId(),
                displayName(entity.getRequesterUserId()),
                entity.getAddresseeUserId(),
                displayName(entity.getAddresseeUserId()),
                entity.getStatus(),
                entity.getRequestedAt(),
                entity.getRespondedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
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
        FriendshipEntity entity = friendshipMapper.findById(friendshipId);
        if (entity == null) {
            throw new CoreException(FRIENDSHIP_NOT_FOUND);
        }
        return new Friendship(
                entity.getId(),
                entity.getRequesterUserId(),
                displayName(entity.getRequesterUserId()),
                entity.getAddresseeUserId(),
                displayName(entity.getAddresseeUserId()),
                entity.getStatus(),
                entity.getRequestedAt(),
                entity.getRespondedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String displayName(String userId) {
        MemberEntity member = memberMapper.findByUserId(userId);
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
