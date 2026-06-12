package com.ssafy.enjoytrip.storage.repository;

import static com.ssafy.enjoytrip.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.domain.NotificationReferenceType.FRIENDSHIP;
import static com.ssafy.enjoytrip.domain.NotificationType.FRIEND_REQUEST_RECEIVED;

import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.repository.NotificationRepository;
import com.ssafy.enjoytrip.storage.entity.NotificationEntity;
import com.ssafy.enjoytrip.storage.jpa.FriendshipJpaRepository;
import com.ssafy.enjoytrip.storage.jpa.NotificationJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class NotificationStorageRepository implements NotificationRepository {
    private final NotificationJpaRepository notificationJpaRepository;
    private final FriendshipJpaRepository friendshipJpaRepository;

    @Override
    public boolean existsByOutboxEventId(Long outboxEventId) {
        return notificationJpaRepository.existsByOutboxEventId(outboxEventId);
    }

    @Override
    public boolean existsUnreadByRecipient(String recipientUserId) {
        return notificationJpaRepository.existsUnreadFriendRequest(
                recipientUserId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                PENDING
        );
    }

    @Override
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
            return toModel(notificationJpaRepository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException duplicate) {
            return notificationJpaRepository.findByOutboxEventId(event.id())
                    .map(entity -> {
                        markReadIfFriendRequestAlreadyHandled(event, entity);
                        return toModel(entity);
                    })
                    .orElseThrow(() -> duplicate);
        }
    }

    @Override
    public List<Notification> findUnreadByRecipient(String recipientUserId, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        List<NotificationEntity> entities = notificationJpaRepository.findUnreadFriendRequests(
                recipientUserId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                PENDING,
                page
        );
        return entities.stream().map(NotificationStorageRepository::toModel).toList();
    }

    @Override
    @Transactional
    public int markReadByReference(String recipientUserId, NotificationReferenceType referenceType, Long referenceId) {
        return notificationJpaRepository.markReadByReference(
                recipientUserId,
                referenceType,
                referenceId,
                LocalDateTime.now()
        );
    }

    private void markReadIfFriendRequestAlreadyHandled(NotificationOutboxEvent event, NotificationEntity entity) {
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

    private static Notification toModel(NotificationEntity entity) {
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
