package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.AUTHENTICATION_REQUIRED;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.service.NotificationService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.NotificationApi;
import com.ssafy.enjoytrip.web.dto.response.NotificationUnreadStatusResponse;
import com.ssafy.enjoytrip.web.dto.response.NotificationsResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {
    private final NotificationService notificationService;

    @GetMapping
    @Override
    public ApiResponse<NotificationsResponse> notifications(@RequestParam(required = false) Integer limit,
                                                            @AuthenticationPrincipal Jwt jwt) {
        List<Notification> notifications = notificationService.findNotifications(authenticatedUserId(jwt), limit);
        return success(NotificationsResponse.from(notifications));
    }

    @GetMapping("/unread-status")
    @Override
    public ApiResponse<NotificationUnreadStatusResponse> unreadStatus(@AuthenticationPrincipal Jwt jwt) {
        return success(new NotificationUnreadStatusResponse(
                notificationService.hasUnreadNotification(authenticatedUserId(jwt))
        ));
    }

    private static String authenticatedUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new CoreException(AUTHENTICATION_REQUIRED);
        }
        return jwt.getSubject().trim();
    }
}
