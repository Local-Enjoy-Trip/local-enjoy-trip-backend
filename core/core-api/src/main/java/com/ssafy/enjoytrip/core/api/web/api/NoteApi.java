package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.request.NearbySectionRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoteCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoteUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoteUpdateTagsRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.SavedNotesRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.NoteResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NotesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Notes", description = "동네핀 쪽지 API")
public interface NoteApi {

    @Operation(
            summary = "쪽지 생성",
            description = "인증 사용자가 지도 좌표에 쪽지를 생성합니다.",
            operationId = "createNote",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteCreateRequest.class),
                            examples = @ExampleObject(value = ApiExamples.NOTE_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "쪽지 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "id": 1,
                                        "title": "서울 산책 메모",
                                        "visibility": "PUBLIC",
                                        "latitude": 37.5665,
                                        "longitude": 126.9780
                                      },
                                      "error": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<NoteResponse> create(
            @Valid NoteCreateRequest request,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(
            summary = "쪽지 수정",
            description = "작성자 본인의 active 쪽지만 수정합니다.",
            operationId = "updateNote",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteUpdateRequest.class),
                            examples = @ExampleObject(value = ApiExamples.NOTE_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "쪽지 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteResponse.class),
                            examples = @ExampleObject(value = ApiExamples.NOTE_RESPONSE)
                    )
            )
    })
    ApiResponse<NoteResponse> update(
            @Parameter(description = "수정할 쪽지 ID", example = "1", required = true) Long id,
            @Valid NoteUpdateRequest request,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(
            summary = "쪽지 삭제",
            description = "작성자 본인의 active 쪽지를 soft delete 합니다.",
            operationId = "deleteNote"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "쪽지 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> delete(
            @Parameter(description = "삭제할 쪽지 ID", example = "1", required = true) Long id,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(summary = "쪽지 저장", description = "인증 사용자가 접근 가능한 active 쪽지를 저장합니다.", operationId = "saveNote")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "쪽지 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> save(
            @Parameter(description = "저장할 쪽지 ID", example = "1", required = true) Long id,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(summary = "쪽지 저장 해제", description = "인증 사용자의 쪽지 저장을 삭제합니다.", operationId = "unsaveNote")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "쪽지 저장 해제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> unsave(
            @Parameter(description = "저장 해제할 쪽지 ID", example = "1", required = true) Long id,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(
            summary = "내가 저장한 쪽지 조회",
            description = "저장 row가 있어도 현재 접근 가능한 ACTIVE 쪽지만 반환합니다.",
            operationId = "getSavedNotes"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "저장 쪽지 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotesResponse.class),
                            examples = @ExampleObject(value = ApiExamples.NOTES_RESPONSE)
                    )
            )
    })
    ApiResponse<NotesResponse> saved(
            @ParameterObject @Valid SavedNotesRequest request,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(
            summary = "주변 최근 쪽지 조회",
            description = """
                    동네핀 홈의 주변 최근 쪽지 섹션을 조회합니다.

                    - 좌표를 전달하지 않으면 서울 시청 좌표(`mapX=126.9780`, `mapY=37.5665`)를 사용합니다.
                    - `radius` 기본값은 500m이며 홈 주변 관광지와 같은 기본 반경입니다.
                    - anonymous viewer는 PUBLIC active note만 볼 수 있습니다.
                    - 인증 viewer는 PUBLIC, 본인 PRIVATE/FRIENDS, accepted 친구의 FRIENDS note를 볼 수 있습니다.
                    - 정렬은 `createdAt desc`, `id desc`입니다.
                    """,
            operationId = "getNearbyNotes"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주변 최근 쪽지 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotesResponse.class),
                            examples = @ExampleObject(value = ApiExamples.NOTES_RESPONSE)
                    )
            )
    })
    ApiResponse<NotesResponse> nearby(
            @ParameterObject @Valid NearbySectionRequest request,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(
            summary = "쪽지 태그 수정",
            description = "작성자 본인의 쪽지 태그를 일괄 수정합니다.",
            operationId = "updateNoteTags",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteUpdateTagsRequest.class)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "쪽지 태그 수정 성공",
                    content = @Content(mediaType = "application/json")
            )
    })
    ApiResponse<Void> updateTags(
            @Parameter(description = "쪽지 ID", example = "1", required = true) Long id,
            @Valid NoteUpdateTagsRequest request,
            @Parameter(hidden = true) Long memberId
    );
}
