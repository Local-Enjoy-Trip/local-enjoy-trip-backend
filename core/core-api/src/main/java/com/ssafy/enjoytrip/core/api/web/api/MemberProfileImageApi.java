package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.api.web.dto.request.ProfileImagePresignedUploadRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.ProfileImageUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.ProfileImagePresignedUploadResponse;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Member Profile Images", description = "회원 프로필 이미지 업로드 API")
public interface MemberProfileImageApi {
    @Operation(
            summary = "회원 프로필 이미지 presigned 업로드 URL 발급",
            description = "인증 사용자에게 프로필 이미지 MinIO/S3-compatible PUT 업로드 URL을 발급합니다.",
            operationId = "createMemberProfileImagePresignedUpload",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    ApiResponse<ProfileImagePresignedUploadResponse> createPresignedUpload(
            ProfileImagePresignedUploadRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );

    @Operation(
            summary = "회원 프로필 이미지 저장",
            description = "업로드된 프로필 이미지 objectKey를 저장하고 public URL은 서버에서 계산합니다.",
            operationId = "updateMemberProfileImage",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    ApiResponse<Void> updateProfileImage(
            ProfileImageUpdateRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );
}
