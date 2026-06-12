package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Notification;
import java.util.List;

public record NotificationsResponse(List<NotificationResponse> notifications) {
    public static NotificationsResponse from(List<Notification> notifications) {
        return new NotificationsResponse(notifications.stream()
                .map(NotificationResponse::from)
                .toList());
    }
}
