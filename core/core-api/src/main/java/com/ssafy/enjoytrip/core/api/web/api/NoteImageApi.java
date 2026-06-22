package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoteImagePresignedUploadRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.NoteImagePresignedUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Note Images", description = "동네핀 쪽지 이미지 업로드 API")
public interface NoteImageApi {
    @Operation(
            summary = "쪽지 이미지 presigned 업로드 URL 발급",
            description = "인증 사용자에게 MinIO/S3-compatible PUT 업로드 URL을 발급합니다.",
            operationId = "createNoteImagePresignedUpload",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteImagePresignedUploadRequest.class),
                            examples = @ExampleObject(value = ApiExamples.NOTE_IMAGE_PRESIGNED_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "쪽지 이미지 presigned 업로드 URL 발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteImagePresignedUploadResponse.class),
                            examples = @ExampleObject(value = ApiExamples.NOTE_IMAGE_PRESIGNED_RESPONSE)
                    )
            )
    })
    ApiResponse<NoteImagePresignedUploadResponse> createPresignedUpload(
            NoteImagePresignedUploadRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );
}
