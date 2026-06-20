package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.core.domain.NotificationReferenceType.FRIENDSHIP;
import static com.ssafy.enjoytrip.core.domain.NotificationType.FRIEND_REQUEST_RECEIVED;

import com.ssafy.enjoytrip.core.domain.Notification;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
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

    public List<Notification> findNotifications(String recipientUserId, int limit) {
        return findUnreadByRecipient(recipientUserId, limit);
    }

    public boolean hasUnreadNotification(String recipientUserId) {
        return notificationMapper.existsUnreadFriendRequest(
                recipientUserId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                PENDING
        ) > 0;
    }

    public Notification saveFriendRequestReceived(Long friendshipId,
                                                  String requesterUserId,
                                                  String recipientUserId) {
        NotificationRecord record = new NotificationRecord(
                recipientUserId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                friendshipId,
                requesterUserId
        );
        markReadIfFriendRequestAlreadyHandled(record);

        notificationMapper.upsertFriendRequest(record);
        return toNotification(notificationMapper.findByBusinessKey(
                recipientUserId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                friendshipId
        ));
    }

    public int markReadByReference(String recipientUserId,
                                   NotificationReferenceType referenceType,
                                   Long referenceId) {
        return notificationMapper.markReadByReference(
                recipientUserId,
                referenceType,
                referenceId,
                LocalDateTime.now()
        );
    }

    private List<Notification> findUnreadByRecipient(String recipientUserId, int limit) {
        return notificationMapper.findUnreadFriendRequests(
                        recipientUserId,
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
                record.getRecipientUserId(),
                record.getType(),
                record.getReferenceType(),
                record.getReferenceId(),
                record.getPayload(),
                record.getReadAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

}
