package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<Notification> findNotifications(String recipientUserId, int limit) {
        return notificationRepository.findUnreadByRecipient(recipientUserId, limit);
    }

    @Transactional(readOnly = true)
    public boolean hasUnreadNotification(String recipientUserId) {
        return notificationRepository.existsUnreadByRecipient(recipientUserId);
    }
}
