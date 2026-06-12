package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_REQUEST;

import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.repository.NotificationRepository;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<Notification> findNotifications(String recipientUserId, Integer limit) {
        return notificationRepository.findUnreadByRecipient(recipientUserId, normalizeLimit(limit));
    }

    @Transactional(readOnly = true)
    public boolean hasUnreadNotification(String recipientUserId) {
        return notificationRepository.existsUnreadByRecipient(recipientUserId);
    }

    private static int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            throw new CoreException(INVALID_REQUEST);
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
