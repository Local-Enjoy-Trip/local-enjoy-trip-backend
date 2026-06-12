package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.NotificationOutboxEvent;
import java.util.Optional;

public interface NotificationOutboxRepository {
    NotificationOutboxEvent saveFriendRequestReceived(Long friendshipId,
                                                       String requesterUserId,
                                                       String recipientUserId);

    Optional<NotificationOutboxEvent> findById(Long id);

    void markProcessed(Long id);

    void markFailed(Long id, String lastError);
}
