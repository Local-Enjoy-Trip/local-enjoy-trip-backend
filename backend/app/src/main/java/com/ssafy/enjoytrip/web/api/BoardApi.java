package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.BoardRequest;
import com.ssafy.enjoytrip.web.dto.response.BoardsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Boards", description = "게시판 게시글 API")
public interface BoardApi {

    @Operation(summary = "게시글 목록 조회", description = "게시판 전체 게시글을 최신 저장 상태 기준으로 조회합니다.", operationId = "findBoardPosts")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "게시글 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardsResponse.class),
                            examples = @ExampleObject(value = """
                                    {"success":true,"data":{"boards":[{"id":"b1","title":"여행 후기","content":"좋았습니다","author":"ssafy","createdAt":"2026-05-20","updatedAt":"2026-05-20"}]},"error":null}
                                    """))
            )
    })
    ApiResponse<BoardsResponse> findAll();

    @Operation(
            summary = "게시글 레거시 액션 처리",
            description = """
                    기존 폼 기반 클라이언트를 위한 통합 엔드포인트입니다.

                    `action=create`는 생성, `action=update`는 수정, `action=delete`는 삭제로 위임됩니다.
                    신규 클라이언트는 `/posts`, `/{id}` REST 엔드포인트 사용을 권장합니다.
                    """,
            operationId = "legacyBoardPost"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "액션 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 action, id 누락 또는 필수 필드 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "수정/삭제 대상 게시글 없음")
    })
    ApiResponse<Void> legacyPost(@ParameterObject BoardRequest request);

    @Operation(summary = "게시글 생성", description = "`id`, `title`, `content`, `author`가 모두 필요합니다.", operationId = "createBoardPost")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 필드 누락")
    })
    ApiResponse<Void> create(@ParameterObject BoardRequest request);

    @Operation(summary = "게시글 수정", description = "경로의 `id` 게시글 제목과 내용을 수정합니다.", operationId = "updateBoardPost")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 필드 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    ApiResponse<Void> update(
            @Parameter(description = "수정할 게시글 ID", example = "b1", required = true) String id,
            @ParameterObject BoardRequest request
    );

    @Operation(summary = "게시글 삭제", description = "경로의 `id` 게시글을 삭제합니다.", operationId = "deleteBoardPost")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "id 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    ApiResponse<Void> delete(@Parameter(description = "삭제할 게시글 ID", example = "b1", required = true) String id);
}
