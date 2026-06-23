package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.core.domain.NotificationReferenceType.FRIENDSHIP;
import static com.ssafy.enjoytrip.core.domain.NotificationType.FRIEND_REQUEST_RECEIVED;

import com.ssafy.enjoytrip.core.domain.Notification;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final FriendshipMapper friendshipMapper;
    private final MemberMapper memberMapper;

    public List<Notification> findNotifications(Long recipientMemberId, int limit) {
        return findUnreadByRecipient(recipientMemberId, limit);
    }

    public boolean hasUnreadNotification(Long recipientMemberId) {
        return notificationMapper.existsUnreadFriendRequest(
                recipientMemberId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                PENDING
        ) > 0;
    }

    public Notification saveFriendRequestReceived(Long friendshipId,
                                                  Long requesterMemberId,
                                                  Long recipientMemberId) {
        NotificationRecord record = new NotificationRecord(
                recipientMemberId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                friendshipId,
                requesterEmail(requesterMemberId)
        );
        markReadIfFriendRequestAlreadyHandled(record);

        notificationMapper.upsertFriendRequest(record);
        return toNotification(notificationMapper.findByBusinessKey(
                recipientMemberId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                friendshipId
        ));
    }

    public int markReadByReference(Long recipientMemberId,
                                   NotificationReferenceType referenceType,
                                   Long referenceId) {
        return notificationMapper.markReadByReference(
                recipientMemberId,
                referenceType,
                referenceId,
                LocalDateTime.now()
        );
    }

    private List<Notification> findUnreadByRecipient(Long recipientMemberId, int limit) {
        return notificationMapper.findUnreadFriendRequests(
                        recipientMemberId,
                        FRIEND_REQUEST_RECEIVED,
                        FRIENDSHIP,
                        PENDING,
                        limit
                ).stream()
                .map(this::toNotification)
                .toList();
    }

    private void markReadIfFriendRequestAlreadyHandled(NotificationRecord record) {
        if (record.getType() != FRIEND_REQUEST_RECEIVED || record.getReferenceType() != FRIENDSHIP) {
            return;
        }
        FriendshipRecord friendship = friendshipMapper.findById(record.getReferenceId());
        if (friendship != null && friendship.getStatus() != PENDING) {
            record.markRead();
        }
    }

    private Notification toNotification(NotificationRecord record) {
        return new Notification(
                record.getId(),
                record.getRecipientMemberId(),
                record.getType(),
                record.getReferenceType(),
                record.getReferenceId(),
                record.getPayload(),
                record.getReadAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private String requesterEmail(Long requesterMemberId) {
        MemberRecord requester = memberMapper.findById(requesterMemberId);
        if (requester == null) {
            return "";
        }
        return requester.getEmail();
    }
}
