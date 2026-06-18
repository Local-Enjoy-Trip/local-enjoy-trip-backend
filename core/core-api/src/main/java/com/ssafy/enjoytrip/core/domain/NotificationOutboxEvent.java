package com.ssafy.enjoytrip.core.domain;

import java.time.LocalDateTime;

public record NotificationOutboxEvent(
        Long id,
        NotificationType eventType,
        String recipientUserId,
        NotificationReferenceType aggregateType,
        Long aggregateId,
        String payload,
        NotificationOutboxStatus status,
        int attemptCount,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime processedAt,
        LocalDateTime updatedAt
) {
    public boolean isPending() {
        return status == NotificationOutboxStatus.PENDING;
    }

    public boolean isProcessed() {
        return status == NotificationOutboxStatus.PROCESSED;
    }
}
