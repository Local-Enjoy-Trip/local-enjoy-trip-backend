package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.core.domain.NotificationReferenceType.FRIENDSHIP;
import static com.ssafy.enjoytrip.core.domain.NotificationType.FRIEND_REQUEST_RECEIVED;

import com.ssafy.enjoytrip.core.domain.Notification;
import com.ssafy.enjoytrip.core.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.storage.db.core.entity.NotificationEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.NotificationOutboxEntity;
import com.ssafy.enjoytrip.storage.db.core.jpa.FriendshipJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.NotificationJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.NotificationOutboxJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationOutboxJpaRepository outboxJpaRepository;
    private final FriendshipJpaRepository friendshipJpaRepository;

    public List<Notification> findNotifications(String recipientUserId, int limit) {
        return findUnreadByRecipient(recipientUserId, limit);
    }

    public boolean hasUnreadNotification(String recipientUserId) {
        return notificationJpaRepository.existsUnreadFriendRequest(
                recipientUserId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                PENDING
        );
    }

    public boolean existsByOutboxEventId(Long outboxEventId) {
        return notificationJpaRepository.existsByOutboxEventId(outboxEventId);
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
            NotificationEntity saved = notificationJpaRepository.saveAndFlush(entity);

            return new Notification(
                    saved.getId(),
                    saved.getRecipientUserId(),
                    saved.getType(),
                    saved.getReferenceType(),
                    saved.getReferenceId(),
                    saved.getPayload(),
                    saved.getOutboxEventId(),
                    saved.getReadAt(),
                    saved.getCreatedAt(),
                    saved.getUpdatedAt()
            );
        } catch (DataIntegrityViolationException duplicate) {
            return notificationJpaRepository.findByOutboxEventId(event.id())
                    .map(entity -> {
                        markReadIfFriendRequestAlreadyHandled(event, entity);
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
                    })
                    .orElseThrow(() -> duplicate);
        }
    }

    private List<Notification> findUnreadByRecipient(String recipientUserId, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        List<NotificationEntity> entities = notificationJpaRepository.findUnreadFriendRequests(
                recipientUserId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                PENDING,
                page
        );
        return entities.stream()
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

    @Transactional
    public int markReadByReference(String recipientUserId,
                                   NotificationReferenceType referenceType,
                                   Long referenceId) {
        return notificationJpaRepository.markReadByReference(
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
        NotificationOutboxEntity saved = outboxJpaRepository.save(entity);

        return new NotificationOutboxEvent(
                saved.getId(),
                saved.getEventType(),
                saved.getRecipientUserId(),
                saved.getAggregateType(),
                saved.getAggregateId(),
                saved.getPayload(),
                saved.getStatus(),
                saved.getAttemptCount(),
                saved.getLastError(),
                saved.getCreatedAt(),
                saved.getProcessedAt(),
                saved.getUpdatedAt()
        );
    }

    public Optional<NotificationOutboxEvent> findOutboxEventById(Long id) {
        return outboxJpaRepository.findById(id)
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
    }

    @Transactional
    public void markOutboxFailed(Long id, String lastError) {
        NotificationOutboxEntity entity = findOutboxEntity(id);
        entity.markFailed(lastError);
    }

    private NotificationOutboxEntity findOutboxEntity(Long id) {
        return outboxJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "알림 outbox를 찾을 수 없습니다: " + id
                ));
    }

    private void markReadIfFriendRequestAlreadyHandled(NotificationOutboxEvent event,
                                                       NotificationEntity entity) {
        if (!isFriendRequestReceived(event)) {
            return;
        }
        friendshipJpaRepository.findById(event.aggregateId())
                .filter(friendship -> friendship.getStatus() != PENDING)
                .ifPresent(ignored -> entity.markRead());
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
