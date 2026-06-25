package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.api.web.dto.request.CourseInviteRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.CourseInvitationResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.CourseInvitationsResponse;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "CourseInvitations", description = "코스 초대 API")
public interface CourseInvitationApi {

    @Operation(
            summary = "친구 초대",
            description = "코스 호스트가 친구를 코스에 초대합니다. 친구 관계인 사용자만 초대할 수 있습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    ApiResponse<CourseInvitationResponse> invite(String courseId,
                                                  CourseInviteRequest request,
                                                  Long authenticatedMemberId);

    @Operation(
            summary = "초대 수락",
            description = "초대받은 사용자가 코스 초대를 수락합니다. 수락 시 코스가 내 코스 목록에 추가됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    ApiResponse<CourseInvitationResponse> accept(String courseId,
                                                  Long invitationId,
                                                  Long authenticatedMemberId);

    @Operation(
            summary = "초대 거절",
            description = "초대받은 사용자가 코스 초대를 거절합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    ApiResponse<CourseInvitationResponse> reject(String courseId,
                                                  Long invitationId,
                                                  Long authenticatedMemberId);

    @Operation(
            summary = "초대 목록 조회",
            description = "코스 호스트가 해당 코스의 초대 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    ApiResponse<CourseInvitationsResponse> list(String courseId, Long authenticatedMemberId);
}
