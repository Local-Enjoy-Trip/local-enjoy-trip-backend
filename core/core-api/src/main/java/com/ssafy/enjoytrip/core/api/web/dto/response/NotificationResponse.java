package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Notification;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        NotificationReferenceType referenceType,
        Long referenceId,
        String payload,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.id(),
                notification.type(),
                notification.referenceType(),
                notification.referenceId(),
                notification.payload(),
                notification.readAt() != null,
                notification.readAt(),
                notification.createdAt()
        );
    }
}
