package com.ssafy.enjoytrip.core.domain.event.listener;

import com.ssafy.enjoytrip.core.domain.event.CourseInvitationSentEvent;
import com.ssafy.enjoytrip.core.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CourseInvitationNotificationEventListener {
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void saveCourseInvitationNotification(CourseInvitationSentEvent event) {
        notificationService.saveCourseInvitationNotification(
                event.invitationId(),
                event.courseTitle(),
                event.inviterMemberId(),
                event.inviteeMemberId()
        );
    }
}
