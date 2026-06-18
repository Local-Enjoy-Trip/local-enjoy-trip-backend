package com.ssafy.enjoytrip.storage.db.core.model;

import com.ssafy.enjoytrip.core.domain.NotificationOutboxStatus;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.NotificationType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutboxRecord extends BaseRecord {
    private Long id;

    private NotificationType eventType;

    private String recipientUserId;

    private NotificationReferenceType aggregateType;

    private Long aggregateId;

    private String payload;

    private NotificationOutboxStatus status = NotificationOutboxStatus.PENDING;

    private int attemptCount;

    private String lastError;

    private LocalDateTime processedAt;

    public NotificationOutboxRecord(NotificationType eventType,
                                    String recipientUserId,
                                    NotificationReferenceType aggregateType,
                                    Long aggregateId,
                                    String payload) {
        this.eventType = eventType;
        this.recipientUserId = recipientUserId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = NotificationOutboxStatus.PENDING;
    }

    public void markProcessed() {
        this.status = NotificationOutboxStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.lastError = null;
    }

    public void markFailed(String errorMessage) {
        this.status = NotificationOutboxStatus.FAILED;
        this.attemptCount += 1;
        this.lastError = abbreviate(errorMessage);
    }

    private static String abbreviate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
