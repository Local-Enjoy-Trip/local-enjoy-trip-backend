package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.request.TagRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.TagsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Tags", description = "태그 관리 API")
public interface TagApi {
    @Operation(summary = "태그 목록 조회", description = "태그 목록을 조회합니다.", operationId = "findTags")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "태그 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TagsResponse.class),
                            examples = @ExampleObject(value = ApiExamples.ATTRACTION_TAGS_RESPONSE)
                    )
            )
    })
    ApiResponse<TagsResponse> tags();

    @Operation(
            summary = "태그 생성",
            description = "태그를 생성합니다.",
            operationId = "createTag",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TagRequest.class),
                            examples = @ExampleObject(value = ApiExamples.TAG_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "태그 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TagsResponse.class),
                            examples = @ExampleObject(value = ApiExamples.ATTRACTION_TAGS_RESPONSE)
                    )
            )
    })
    ApiResponse<TagsResponse> create(TagRequest request);

    @Operation(
            summary = "태그 수정",
            description = "태그 이름을 수정합니다.",
            operationId = "updateTag",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TagRequest.class),
                            examples = @ExampleObject(value = ApiExamples.TAG_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "태그 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> update(
            @Parameter(description = "수정할 태그 ID", example = "1", required = true) Long id,
            TagRequest request
    );

    @Operation(summary = "태그 삭제", description = "태그를 삭제합니다.", operationId = "deleteTag")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "태그 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> delete(@Parameter(description = "삭제할 태그 ID", example = "1", required = true) Long id);
}
