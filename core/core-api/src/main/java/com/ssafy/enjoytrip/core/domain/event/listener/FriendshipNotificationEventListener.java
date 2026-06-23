package com.ssafy.enjoytrip.core.domain.event.listener;

import com.ssafy.enjoytrip.core.domain.event.FriendshipRequestedEvent;
import com.ssafy.enjoytrip.core.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FriendshipNotificationEventListener {
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void saveFriendRequestNotification(FriendshipRequestedEvent event) {
        notificationService.saveFriendRequestReceived(
                event.friendshipId(),
                event.requesterMemberId(),
                event.recipientMemberId()
        );
    }
}
