package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.response.NotificationUnreadStatusResponse;
import com.ssafy.enjoytrip.web.dto.response.NotificationsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notifications", description = "동네핀 저장형 인앱 알림 API")
public interface NotificationApi {
    @Operation(
            summary = "내 미읽음 알림 조회",
            description = "현재 알림은 친구 요청 알림만 제공하며, 프론트가 별도 읽음 처리를 고민하지 않도록 기본적으로 미읽음 알림만 반환합니다. 여기서 미읽음은 요청을 수락하거나 거절하지 않은 상태의 친구 요청 알림을 의미합니다.",
            operationId = "getNotifications"
    )
    ApiResponse<NotificationsResponse> notifications(int limit, @Parameter(hidden = true) String authenticatedUserId);

    @Operation(
            summary = "미읽음 알림 존재 여부 조회",
            description = "친구 요청을 수락하거나 거절하지 않아 아직 처리되지 않은 알림이 있는지 반환합니다. 별도 알림 읽음 API는 제공하지 않으며, 친구 요청 수락/거절 액션이 알림을 함께 처리합니다.",
            operationId = "getUnreadNotificationStatus"
    )
    ApiResponse<NotificationUnreadStatusResponse> unreadStatus(@Parameter(hidden = true) String authenticatedUserId);
}
