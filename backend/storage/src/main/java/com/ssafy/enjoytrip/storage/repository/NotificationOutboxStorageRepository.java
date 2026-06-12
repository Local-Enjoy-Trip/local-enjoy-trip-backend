package com.ssafy.enjoytrip.storage.repository;

import static com.ssafy.enjoytrip.domain.NotificationReferenceType.FRIENDSHIP;
import static com.ssafy.enjoytrip.domain.NotificationType.FRIEND_REQUEST_RECEIVED;

import com.ssafy.enjoytrip.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.repository.NotificationOutboxRepository;
import com.ssafy.enjoytrip.storage.entity.NotificationOutboxEntity;
import com.ssafy.enjoytrip.storage.jpa.NotificationOutboxJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class NotificationOutboxStorageRepository implements NotificationOutboxRepository {
    private final NotificationOutboxJpaRepository outboxJpaRepository;

    @Override
    @Transactional
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
        return toModel(outboxJpaRepository.save(entity));
    }

    @Override
    public Optional<NotificationOutboxEvent> findById(Long id) {
        return outboxJpaRepository.findById(id).map(NotificationOutboxStorageRepository::toModel);
    }

    @Override
    @Transactional
    public void markProcessed(Long id) {
        NotificationOutboxEntity entity = outboxJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Notification outbox not found: " + id));
        entity.markProcessed();
    }

    @Override
    @Transactional
    public void markFailed(Long id, String lastError) {
        NotificationOutboxEntity entity = outboxJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Notification outbox not found: " + id));
        entity.markFailed(lastError);
    }

    private static NotificationOutboxEvent toModel(NotificationOutboxEntity entity) {
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

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
