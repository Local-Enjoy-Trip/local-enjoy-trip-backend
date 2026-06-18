package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.core.domain.NotificationReferenceType.FRIENDSHIP;
import static com.ssafy.enjoytrip.core.domain.NotificationType.FRIEND_REQUEST_RECEIVED;

import com.ssafy.enjoytrip.core.domain.Notification;
import com.ssafy.enjoytrip.core.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.storage.db.core.entity.FriendshipEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.NotificationEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.NotificationOutboxEntity;
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
            NotificationEntity entity = new NotificationEntity(
                    event.recipientUserId(),
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.payload(),
                    event.id()
            );
            markReadIfFriendRequestAlreadyHandled(event, entity);
            notificationMapper.insert(entity);
            return new Notification(
                entity.getId(),
                entity.getRecipientUserId(),
                entity.getType(),
                entity.getReferenceType(),
                entity.getReferenceId(),
                entity.getPayload(),
                entity.getOutboxEventId(),
                entity.getReadAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
        } catch (DataIntegrityViolationException duplicate) {
            NotificationEntity entity = notificationMapper.findByOutboxEventId(event.id());
            if (entity == null) {
                throw duplicate;
            }
            markReadIfFriendRequestAlreadyHandled(event, entity);
            notificationMapper.updateReadAt(entity);
            return new Notification(
                entity.getId(),
                entity.getRecipientUserId(),
                entity.getType(),
                entity.getReferenceType(),
                entity.getReferenceId(),
                entity.getPayload(),
                entity.getOutboxEventId(),
                entity.getReadAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
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
        NotificationOutboxEntity entity = new NotificationOutboxEntity(
                FRIEND_REQUEST_RECEIVED,
                recipientUserId,
                FRIENDSHIP,
                friendshipId,
                payload
        );
        outboxMapper.insert(entity);
        return new NotificationOutboxEvent(
                entity.getId(),
                entity.getEventType(),
                entity.getRecipientUserId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getPayload(),
                entity.getStatus(),
                entity.getAttemptCount(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getProcessedAt(),
                entity.getUpdatedAt()
        );
    }

    public Optional<NotificationOutboxEvent> findOutboxEventById(Long id) {
        return Optional.ofNullable(outboxMapper.findById(id))
                .map(entity -> new NotificationOutboxEvent(
                                entity.getId(),
                                entity.getEventType(),
                                entity.getRecipientUserId(),
                                entity.getAggregateType(),
                                entity.getAggregateId(),
                                entity.getPayload(),
                                entity.getStatus(),
                                entity.getAttemptCount(),
                                entity.getLastError(),
                                entity.getCreatedAt(),
                                entity.getProcessedAt(),
                                entity.getUpdatedAt()
                        ));
    }

    @Transactional
    public void markOutboxProcessed(Long id) {
        NotificationOutboxEntity entity = findOutboxEntity(id);
        entity.markProcessed();
        outboxMapper.markProcessed(entity);
    }

    @Transactional
    public void markOutboxFailed(Long id, String lastError) {
        NotificationOutboxEntity entity = findOutboxEntity(id);
        entity.markFailed(lastError);
        outboxMapper.markFailed(entity);
    }

    private List<Notification> findUnreadByRecipient(String recipientUserId, int limit) {
        return notificationMapper.findUnreadFriendRequests(
                        recipientUserId,
                        FRIEND_REQUEST_RECEIVED,
                        FRIENDSHIP,
                        PENDING,
                        limit
                ).stream()
                .map(entity -> new Notification(
                                entity.getId(),
                                entity.getRecipientUserId(),
                                entity.getType(),
                                entity.getReferenceType(),
                                entity.getReferenceId(),
                                entity.getPayload(),
                                entity.getOutboxEventId(),
                                entity.getReadAt(),
                                entity.getCreatedAt(),
                                entity.getUpdatedAt()
                        ))
                .toList();
    }

    private NotificationOutboxEntity findOutboxEntity(Long id) {
        NotificationOutboxEntity entity = outboxMapper.findById(id);
        if (entity == null) {
            throw new IllegalStateException("알림 outbox를 찾을 수 없습니다: " + id);
        }
        return entity;
    }

    private void markReadIfFriendRequestAlreadyHandled(NotificationOutboxEvent event,
                                                       NotificationEntity entity) {
        if (!isFriendRequestReceived(event)) {
            return;
        }
        FriendshipEntity friendship = friendshipMapper.findById(event.aggregateId());
        if (friendship != null && friendship.getStatus() != PENDING) {
            entity.markRead();
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
