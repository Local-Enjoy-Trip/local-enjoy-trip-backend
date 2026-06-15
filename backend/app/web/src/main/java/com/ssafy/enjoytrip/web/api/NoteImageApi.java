package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.NoteImagePresignedUploadRequest;
import com.ssafy.enjoytrip.web.dto.response.NoteImagePresignedUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Note Images", description = "동네핀 쪽지 이미지 업로드 API")
public interface NoteImageApi {
    @Operation(
            summary = "쪽지 이미지 presigned 업로드 URL 발급",
            description = "인증 사용자에게 MinIO/S3-compatible PUT 업로드 URL을 발급합니다.",
            operationId = "createNoteImagePresignedUpload"
    )
    ApiResponse<NoteImagePresignedUploadResponse> createPresignedUpload(
            NoteImagePresignedUploadRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );
}
