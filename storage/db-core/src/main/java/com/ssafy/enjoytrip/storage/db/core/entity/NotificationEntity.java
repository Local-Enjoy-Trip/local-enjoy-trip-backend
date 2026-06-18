package com.ssafy.enjoytrip.storage.db.core.entity;

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
public class NotificationEntity extends BaseEntity {
    private Long id;

    private String recipientUserId;

    private NotificationType type;

    private NotificationReferenceType referenceType;

    private Long referenceId;

    private String payload;

    private LocalDateTime readAt;

    private Long outboxEventId;

    public NotificationEntity(String recipientUserId,
                              NotificationType type,
                              NotificationReferenceType referenceType,
                              Long referenceId,
                              String payload,
                              Long outboxEventId) {
        this.recipientUserId = recipientUserId;
        this.type = type;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.payload = payload;
        this.outboxEventId = outboxEventId;
    }

    public void markRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }
}
