package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.domain.NotificationOutboxStatus.PENDING;
import static com.ssafy.enjoytrip.domain.NotificationOutboxStatus.PROCESSED;
import static com.ssafy.enjoytrip.domain.NotificationReferenceType.FRIENDSHIP;
import static com.ssafy.enjoytrip.domain.NotificationType.FRIEND_REQUEST_RECEIVED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.domain.NotificationOutboxStatus;
import com.ssafy.enjoytrip.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.repository.NotificationOutboxRepository;
import com.ssafy.enjoytrip.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationOutboxProcessorTest {

    @DisplayName("대기 outbox 이벤트를 notification으로 저장하고 processed 처리한다")
    @Test
    void processPendingOutboxCreatesNotificationAndMarksProcessed() {
        FakeOutboxRepository outbox = new FakeOutboxRepository(outboxEvent(10L, PENDING));
        FakeNotificationRepository notifications = new FakeNotificationRepository();
        NotificationOutboxProcessor processor = new NotificationOutboxProcessor(outbox, notifications);

        processor.processOutboxEvent(10L);

        assertEquals(10L, notifications.savedOutboxId);
        assertEquals(10L, outbox.processedId);
    }

    @DisplayName("이미 생성된 outbox notification은 중복 저장하지 않고 processed로 수렴한다")
    @Test
    void processOutboxIsIdempotentWhenNotificationExists() {
        FakeOutboxRepository outbox = new FakeOutboxRepository(outboxEvent(10L, PENDING));
        FakeNotificationRepository notifications = new FakeNotificationRepository();
        notifications.exists = true;
        NotificationOutboxProcessor processor = new NotificationOutboxProcessor(outbox, notifications);

        processor.processOutboxEvent(10L);

        assertEquals(null, notifications.savedOutboxId);
        assertEquals(10L, outbox.processedId);
    }

    @DisplayName("processed outbox 이벤트는 재처리해도 아무 작업을 하지 않는다")
    @Test
    void processedOutboxIsIgnored() {
        FakeOutboxRepository outbox = new FakeOutboxRepository(outboxEvent(10L, PROCESSED));
        FakeNotificationRepository notifications = new FakeNotificationRepository();
        NotificationOutboxProcessor processor = new NotificationOutboxProcessor(outbox, notifications);

        processor.processOutboxEvent(10L);

        assertEquals(null, notifications.savedOutboxId);
        assertEquals(null, outbox.processedId);
    }

    private static NotificationOutboxEvent outboxEvent(Long id, NotificationOutboxStatus status) {
        return new NotificationOutboxEvent(
                id,
                FRIEND_REQUEST_RECEIVED,
                "bob",
                FRIENDSHIP,
                1L,
                "{}",
                status,
                0,
                null,
                LocalDateTime.of(2026, 6, 12, 10, 0),
                null,
                null
        );
    }

    private static class FakeOutboxRepository implements NotificationOutboxRepository {
        private final NotificationOutboxEvent event;
        private Long processedId;

        FakeOutboxRepository(NotificationOutboxEvent event) {
            this.event = event;
        }

        @Override
        public NotificationOutboxEvent saveFriendRequestReceived(Long friendshipId,
                                                                 String requesterUserId,
                                                                 String requesterDisplayName,
                                                                 String recipientUserId) {
            return null;
        }

        @Override
        public Optional<NotificationOutboxEvent> findById(Long id) {
            return Optional.of(event);
        }

        @Override
        public void markProcessed(Long id) {
            this.processedId = id;
        }

        @Override
        public void markFailed(Long id, String lastError) {
        }
    }

    private static class FakeNotificationRepository implements NotificationRepository {
        private boolean exists;
        private Long savedOutboxId;

        @Override
        public boolean existsByOutboxEventId(Long outboxEventId) {
            return exists;
        }

        @Override
        public boolean existsUnreadByRecipient(String recipientUserId) {
            return false;
        }

        @Override
        public Notification saveFromOutbox(NotificationOutboxEvent event) {
            savedOutboxId = event.id();
            return null;
        }

        @Override
        public List<Notification> findUnreadByRecipient(String recipientUserId, int limit) {
            return List.of();
        }

        @Override
        public int markReadByReference(String recipientUserId, NotificationReferenceType referenceType, Long referenceId) {
            return 0;
        }
    }
}
