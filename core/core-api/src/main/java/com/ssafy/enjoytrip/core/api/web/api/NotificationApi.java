package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NotificationUnreadStatusResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NotificationsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

@Tag(name = "Notifications", description = "동네핀 저장형 인앱 알림 API")
@Validated
public interface NotificationApi {
    @Operation(
            summary = "내 미읽음 알림 조회",
            description = """
                    현재 알림은 친구 요청 알림만 제공합니다.
                    프론트가 별도 읽음 처리를 고민하지 않도록 기본적으로 미읽음 알림만 반환합니다.
                    여기서 미읽음은 요청을 수락하거나 거절하지 않은 상태의 친구 요청 알림을 의미합니다.
                    """,
            operationId = "getNotifications"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 미읽음 알림 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationsResponse.class),
                            examples = @ExampleObject(value = ApiExamples.NOTIFICATIONS_RESPONSE)
                    )
            )
    })
    ApiResponse<NotificationsResponse> notifications(
            @Parameter(description = "조회 개수", example = "20") @Min(1) int limit,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(
            summary = "미읽음 알림 존재 여부 조회",
            description = """
                    친구 요청을 수락하거나 거절하지 않아 아직 처리되지 않은 알림이 있는지 반환합니다.
                    별도 알림 읽음 API는 제공하지 않으며, 친구 요청 수락/거절 액션이 알림을 함께 처리합니다.
                    """,
            operationId = "getUnreadNotificationStatus"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "미읽음 알림 존재 여부 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationUnreadStatusResponse.class),
                            examples = @ExampleObject(value = ApiExamples.NOTIFICATION_UNREAD_STATUS_RESPONSE)
                    )
            )
    })
    ApiResponse<NotificationUnreadStatusResponse> unreadStatus(
            @Parameter(hidden = true) Long memberId
    );
}
