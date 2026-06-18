package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.core.domain.NotificationReferenceType.FRIENDSHIP;
import static com.ssafy.enjoytrip.core.domain.NotificationType.FRIEND_REQUEST_RECEIVED;

import com.ssafy.enjoytrip.core.domain.Notification;
import com.ssafy.enjoytrip.core.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationOutboxRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationOutboxMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final NotificationOutboxMapper outboxMapper;
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

    public boolean existsByOutboxEventId(Long outboxEventId) {
        return notificationMapper.existsByOutboxEventId(outboxEventId) > 0;
    }

    @Transactional
    public Notification saveFromOutbox(NotificationOutboxEvent event) {
        try {
            NotificationRecord record = new NotificationRecord(
                    event.recipientUserId(),
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.payload(),
                    event.id()
            );
            markReadIfFriendRequestAlreadyHandled(event, record);
            notificationMapper.insert(record);
            return new Notification(
                record.getId(),
                record.getRecipientUserId(),
                record.getType(),
                record.getReferenceType(),
                record.getReferenceId(),
                record.getPayload(),
                record.getOutboxEventId(),
                record.getReadAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
        } catch (DataIntegrityViolationException duplicate) {
            NotificationRecord record = notificationMapper.findByOutboxEventId(event.id());
            if (record == null) {
                throw duplicate;
            }
            markReadIfFriendRequestAlreadyHandled(event, record);
            notificationMapper.updateReadAt(record);
            return new Notification(
                record.getId(),
                record.getRecipientUserId(),
                record.getType(),
                record.getReferenceType(),
                record.getReferenceId(),
                record.getPayload(),
                record.getOutboxEventId(),
                record.getReadAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
        }
    }

    @Transactional
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

    public NotificationOutboxEvent saveFriendRequestReceived(Long friendshipId,
                                                             String requesterUserId,
                                                             String recipientUserId) {
        String payload = "{\"requesterUserId\":\"" + escape(requesterUserId) + "\","
                + "\"friendshipId\":" + friendshipId + "}";
        NotificationOutboxRecord record = new NotificationOutboxRecord(
                FRIEND_REQUEST_RECEIVED,
                recipientUserId,
                FRIENDSHIP,
                friendshipId,
                payload
        );
        outboxMapper.insert(record);
        return new NotificationOutboxEvent(
                record.getId(),
                record.getEventType(),
                record.getRecipientUserId(),
                record.getAggregateType(),
                record.getAggregateId(),
                record.getPayload(),
                record.getStatus(),
                record.getAttemptCount(),
                record.getLastError(),
                record.getCreatedAt(),
                record.getProcessedAt(),
                record.getUpdatedAt()
        );
    }

    public Optional<NotificationOutboxEvent> findOutboxEventById(Long id) {
        return Optional.ofNullable(outboxMapper.findById(id))
                .map(record -> new NotificationOutboxEvent(
                                record.getId(),
                                record.getEventType(),
                                record.getRecipientUserId(),
                                record.getAggregateType(),
                                record.getAggregateId(),
                                record.getPayload(),
                                record.getStatus(),
                                record.getAttemptCount(),
                                record.getLastError(),
                                record.getCreatedAt(),
                                record.getProcessedAt(),
                                record.getUpdatedAt()
                        ));
    }

    @Transactional
    public void markOutboxProcessed(Long id) {
        NotificationOutboxRecord record = findOutboxRecord(id);
        record.markProcessed();
        outboxMapper.markProcessed(record);
    }

    @Transactional
    public void markOutboxFailed(Long id, String lastError) {
        NotificationOutboxRecord record = findOutboxRecord(id);
        record.markFailed(lastError);
        outboxMapper.markFailed(record);
    }

    private List<Notification> findUnreadByRecipient(String recipientUserId, int limit) {
        return notificationMapper.findUnreadFriendRequests(
                        recipientUserId,
                        FRIEND_REQUEST_RECEIVED,
                        FRIENDSHIP,
                        PENDING,
                        limit
                ).stream()
                .map(record -> new Notification(
                                record.getId(),
                                record.getRecipientUserId(),
                                record.getType(),
                                record.getReferenceType(),
                                record.getReferenceId(),
                                record.getPayload(),
                                record.getOutboxEventId(),
                                record.getReadAt(),
                                record.getCreatedAt(),
                                record.getUpdatedAt()
                        ))
                .toList();
    }

    private NotificationOutboxRecord findOutboxRecord(Long id) {
        NotificationOutboxRecord record = outboxMapper.findById(id);
        if (record == null) {
            throw new IllegalStateException("알림 outbox를 찾을 수 없습니다: " + id);
        }
        return record;
    }

    private void markReadIfFriendRequestAlreadyHandled(NotificationOutboxEvent event,
                                                       NotificationRecord record) {
        if (!isFriendRequestReceived(event)) {
            return;
        }
        FriendshipRecord friendship = friendshipMapper.findById(event.aggregateId());
        if (friendship != null && friendship.getStatus() != PENDING) {
            record.markRead();
        }
    }

    private static boolean isFriendRequestReceived(NotificationOutboxEvent event) {
        return event.eventType() == FRIEND_REQUEST_RECEIVED && event.aggregateType() == FRIENDSHIP;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
