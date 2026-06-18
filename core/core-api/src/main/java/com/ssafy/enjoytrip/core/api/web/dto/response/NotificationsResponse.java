package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Notification;
import java.util.List;

public record NotificationsResponse(List<NotificationResponse> notifications) {
    public static NotificationsResponse from(List<Notification> notifications) {
        return new NotificationsResponse(notifications.stream()
                .map(NotificationResponse::from)
                .toList());
    }
}
