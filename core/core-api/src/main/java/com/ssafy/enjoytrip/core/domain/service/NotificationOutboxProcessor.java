package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.NOTIFICATION_OUTBOX_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationOutboxProcessor {
    private final NotificationService notificationService;

    @Transactional
    public void processOutboxEvent(Long outboxEventId) {
        NotificationOutboxEvent event = notificationService.findOutboxEventById(outboxEventId)
                .orElseThrow(() -> new CoreException(NOTIFICATION_OUTBOX_NOT_FOUND));
        if (event.isProcessed()) {
            return;
        }
        if (!event.isPending()) {
            return;
        }

        if (!notificationService.existsByOutboxEventId(outboxEventId)) {
            notificationService.saveFromOutbox(event);
        }
        notificationService.markOutboxProcessed(outboxEventId);
    }

    public void markFailed(Long outboxEventId, String errorMessage) {
        notificationService.markOutboxFailed(outboxEventId, errorMessage);
    }
}
