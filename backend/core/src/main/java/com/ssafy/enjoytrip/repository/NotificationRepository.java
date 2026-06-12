package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.domain.NotificationReferenceType;
import java.util.List;

public interface NotificationRepository {
    boolean existsByOutboxEventId(Long outboxEventId);

    boolean existsUnreadByRecipient(String recipientUserId);

    Notification saveFromOutbox(NotificationOutboxEvent event);

    List<Notification> findUnreadByRecipient(String recipientUserId, int limit);

    int markReadByReference(String recipientUserId, NotificationReferenceType referenceType, Long referenceId);
}
