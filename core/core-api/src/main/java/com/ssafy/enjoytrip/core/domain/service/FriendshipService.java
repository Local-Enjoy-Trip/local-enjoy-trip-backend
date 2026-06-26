package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.ACCEPTED;
import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.DELETED;
import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.REJECTED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.FRIENDSHIP_ALREADY_ACTIVE;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.FRIENDSHIP_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Friendship;
import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.event.FriendshipRequestedEvent;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipMapper friendshipMapper;
    private final MemberMapper memberMapper;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Friendship requestFriendship(Long requesterMemberId, Long targetUserId) {
        MemberRecord target = findRequiredMemberById(targetUserId);
        Friendship.validateRequestableMembers(requesterMemberId, target.getId());
        if (existsActiveBetween(requesterMemberId, target.getId())) {
            throw new CoreException(FRIENDSHIP_ALREADY_ACTIVE);
        }

        Friendship friendship = savePending(requesterMemberId, target.getId());
        publishFriendshipRequested(friendship);

        return friendship;
    }

    @Transactional
    public Friendship acceptRequest(Long friendshipId, Long actorMemberId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateAcceptableBy(actorMemberId);
        Friendship accepted = updateStatus(friendshipId, ACCEPTED);
        markFriendRequestNotificationRead(accepted);
        return accepted;
    }

    @Transactional
    public Friendship rejectRequest(Long friendshipId, Long actorMemberId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateRejectableBy(actorMemberId);
        Friendship rejected = updateStatus(friendshipId, REJECTED);
        markFriendRequestNotificationRead(rejected);
        return rejected;
    }

    @Transactional
    public Friendship cancelSentRequest(Long friendshipId, Long actorMemberId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateCancelableBy(actorMemberId);
        return updateStatus(friendshipId, DELETED);
    }

    @Transactional
    public Friendship deleteFriendship(Long friendshipId, Long actorMemberId) {
        Friendship friendship = findFriendship(friendshipId);
        friendship.validateDeletableBy(actorMemberId);
        return updateStatus(friendshipId, DELETED);
    }

    public List<Friendship> findFriends(Long actorMemberId) {
        return friendshipMapper.findByParticipantAndStatus(actorMemberId, ACCEPTED).stream()
                .map(this::toFriendship)
                .toList();
    }

    public List<Friendship> findReceivedPendingRequests(Long actorMemberId) {
        return friendshipMapper.findReceivedRequests(actorMemberId, PENDING).stream()
                .map(this::toFriendship)
                .toList();
    }

    public List<Friendship> findSentPendingRequests(Long actorMemberId) {
        return friendshipMapper.findSentRequests(actorMemberId, PENDING).stream()
                .map(this::toFriendship)
                .toList();
    }

    public Optional<Friendship> findById(Long id) {
        return Optional.ofNullable(friendshipMapper.findById(id))
                .map(this::toFriendship);
    }

    public boolean existsActiveBetween(Long memberId, Long otherMemberId) {
        return friendshipMapper.existsActiveBetween(
                memberId,
                otherMemberId,
                List.of(PENDING, ACCEPTED)
        ) > 0;
    }

    private Friendship savePending(Long requesterMemberId, Long addresseeMemberId) {
        FriendshipRecord record = new FriendshipRecord(requesterMemberId, addresseeMemberId);
        friendshipMapper.insert(record);
        return toFriendship(record);
    }

    private Friendship updateStatus(Long id, FriendshipStatus status) {
        FriendshipRecord record = friendshipMapper.findById(id);
        if (record == null) {
            throw new IllegalStateException("친구 관계를 찾을 수 없습니다: " + id);
        }
        record.transitionTo(status);
        friendshipMapper.updateStatus(record);
        return toFriendship(record);
    }

    private void publishFriendshipRequested(Friendship friendship) {
        eventPublisher.publishEvent(new FriendshipRequestedEvent(
                friendship.id(),
                friendship.requesterMemberId(),
                friendship.addresseeMemberId()
        ));
    }

    private void markFriendRequestNotificationRead(Friendship friendship) {
        notificationMapper.markReadByReference(
                friendship.addresseeMemberId(),
                NotificationReferenceType.FRIENDSHIP,
                friendship.id(),
                LocalDateTime.now()
        );
    }

    private Friendship findFriendship(Long friendshipId) {
        FriendshipRecord record = friendshipMapper.findById(friendshipId);
        if (record == null) {
            throw new CoreException(FRIENDSHIP_NOT_FOUND);
        }
        return toFriendship(record);
    }

    private Friendship toFriendship(FriendshipRecord record) {
        MemberRecord requester = findMemberById(record.getRequesterMemberId());
        MemberRecord addressee = findMemberById(record.getAddresseeMemberId());
        return new Friendship(
                record.getId(),
                record.getRequesterMemberId(),
                emailOrEmpty(requester),
                displayName(requester),
                record.getAddresseeMemberId(),
                emailOrEmpty(addressee),
                displayName(addressee),
                record.getStatus(),
                record.getRequestedAt(),
                record.getRespondedAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private MemberRecord findRequiredMemberById(Long memberId) {
        MemberRecord member = memberMapper.findById(memberId);
        if (member == null) {
            throw new CoreException(USER_NOT_FOUND);
        }
        return member;
    }

    private MemberRecord findMemberById(Long memberId) {
        return memberMapper.findById(memberId);
    }

    private static String emailOrEmpty(MemberRecord member) {
        return member == null ? "" : member.getEmail();
    }

    private static String displayName(MemberRecord member) {
        if (member == null) {
            return "";
        }
        if (member.getNickname() != null && !member.getNickname().isBlank()) {
            return member.getNickname();
        }
        if (member.getName() != null && !member.getName().isBlank()) {
            return member.getName();
        }
        return member.getEmail();
    }
}
