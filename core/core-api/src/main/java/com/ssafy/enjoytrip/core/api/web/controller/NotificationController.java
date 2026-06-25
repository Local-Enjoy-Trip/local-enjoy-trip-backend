package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import com.ssafy.enjoytrip.core.api.web.api.NotificationApi;
import com.ssafy.enjoytrip.core.api.web.dto.response.NotificationUnreadStatusResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NotificationsResponse;
import com.ssafy.enjoytrip.core.domain.Notification;
import com.ssafy.enjoytrip.core.domain.service.NotificationService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {
    private static final int MAX_LIMIT = 100;

    private final NotificationService notificationService;

    @GetMapping
    @Override
    public ApiResponse<NotificationsResponse> notifications(
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticatedMemberId Long memberId
    ) {
        List<Notification> notifications = notificationService.findNotifications(
                memberId,
                Math.min(limit, MAX_LIMIT)
        );
        return success(NotificationsResponse.from(notifications));
    }

    @GetMapping("/unread-status")
    @Override
    public ApiResponse<NotificationUnreadStatusResponse> unreadStatus(@AuthenticatedMemberId Long memberId) {
        return success(new NotificationUnreadStatusResponse(
                notificationService.hasUnreadNotification(memberId)
        ));
    }
}
