package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.api.web.dto.request.ProfileImagePresignedUploadRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.ProfileImageUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.ProfileImagePresignedUploadResponse;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Member Profile Images", description = "회원 프로필 이미지 업로드 API")
public interface MemberProfileImageApi {
    @Operation(
            summary = "회원 프로필 이미지 presigned 업로드 URL 발급",
            description = "인증 사용자에게 프로필 이미지 MinIO/S3-compatible PUT 업로드 URL을 발급합니다.",
            operationId = "createMemberProfileImagePresignedUpload",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfileImagePresignedUploadRequest.class),
                            examples = @ExampleObject(value = ApiExamples.PROFILE_IMAGE_PRESIGNED_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 이미지 presigned 업로드 URL 발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfileImagePresignedUploadResponse.class),
                            examples = @ExampleObject(value = ApiExamples.PRESIGNED_PROFILE_IMAGE_RESPONSE)
                    )
            )
    })
    ApiResponse<ProfileImagePresignedUploadResponse> createPresignedUpload(
            ProfileImagePresignedUploadRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );

    @Operation(
            summary = "회원 프로필 이미지 저장",
            description = "업로드된 프로필 이미지 objectKey를 저장하고 public URL은 서버에서 계산합니다.",
            operationId = "updateMemberProfileImage",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfileImageUpdateRequest.class),
                            examples = @ExampleObject(value = ApiExamples.PROFILE_IMAGE_UPDATE_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 이미지 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> updateProfileImage(
            ProfileImageUpdateRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );
}
