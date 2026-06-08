package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.NoticeRequest;
import com.ssafy.enjoytrip.web.dto.response.NoticesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Notices", description = "공지사항 API")
public interface NoticeApi {

    @Operation(summary = "공지사항 목록 조회", description = "등록된 공지사항 전체 목록을 조회합니다.", operationId = "findNotices")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "공지사항 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticesResponse.class),
                            examples = @ExampleObject(value = """
                                    {"success":true,"data":{"notices":[{"id":1,"title":"공지","content":"내용","author":"admin","createdAt":"2026-05-20","updatedAt":"2026-05-20"}]},"error":null}
                                    """))
            )
    })
    ApiResponse<NoticesResponse> findAll();

    @Operation(
            summary = "공지사항 레거시 액션 처리",
            description = "`action=create`는 생성, `action=update`는 수정, `action=delete`는 삭제로 위임됩니다. 신규 클라이언트는 `/items`, `/{id}` REST 엔드포인트 사용을 권장합니다.",
            operationId = "legacyNoticePost"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "액션 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 action, id 오류 또는 필수 필드 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항 없음")
    })
    ApiResponse<Void> legacyPost(@ParameterObject NoticeRequest request);

    @Operation(summary = "공지사항 생성", description = "`title`, `content`, `author`가 모두 필요합니다.", operationId = "createNotice")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공지사항 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 필드 누락")
    })
    ApiResponse<Void> create(@ParameterObject NoticeRequest request);

    @Operation(summary = "공지사항 수정", description = "경로의 `id` 공지사항 제목과 내용을 수정합니다.", operationId = "updateNotice")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공지사항 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 id 또는 필수 필드 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항 없음")
    })
    ApiResponse<Void> update(
            @Parameter(description = "수정할 공지사항 ID", example = "1", required = true) Long id,
            @ParameterObject NoticeRequest request
    );

    @Operation(summary = "공지사항 삭제", description = "경로의 `id` 공지사항을 삭제합니다.", operationId = "deleteNotice")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공지사항 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 id"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항 없음")
    })
    ApiResponse<Void> delete(@Parameter(description = "삭제할 공지사항 ID", example = "1", required = true) Long id);
}
