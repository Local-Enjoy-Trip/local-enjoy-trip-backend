package com.ssafy.enjoytrip.storage.db.core.model;

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
public class NotificationRecord extends BaseRecord {
    private Long id;

    private Long recipientMemberId;

    private NotificationType type;

    private NotificationReferenceType referenceType;

    private Long referenceId;

    private String payload;

    private LocalDateTime readAt;

    public NotificationRecord(Long recipientMemberId,
                              NotificationType type,
                              NotificationReferenceType referenceType,
                              Long referenceId,
                              String payload) {
        this.recipientMemberId = recipientMemberId;
        this.type = type;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.payload = payload;
    }

    public void markRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }
}
