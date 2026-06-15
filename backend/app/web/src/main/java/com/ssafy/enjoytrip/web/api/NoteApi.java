package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.NearbySectionRequest;
import com.ssafy.enjoytrip.web.dto.request.NoteCreateRequest;
import com.ssafy.enjoytrip.web.dto.request.NoteUpdateRequest;
import com.ssafy.enjoytrip.web.dto.response.NoteResponse;
import com.ssafy.enjoytrip.web.dto.response.NotesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Notes", description = "동네핀 쪽지 API")
public interface NoteApi {

    @Operation(summary = "쪽지 생성", description = "인증 사용자가 지도 좌표에 쪽지를 생성합니다.", operationId = "createNote")
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
    ApiResponse<NoteResponse> create(NoteCreateRequest request, @Parameter(hidden = true) String authenticatedUserId);

    @Operation(summary = "쪽지 수정", description = "작성자 본인의 active 쪽지만 수정합니다.", operationId = "updateNote")
    ApiResponse<NoteResponse> update(Long id, NoteUpdateRequest request, @Parameter(hidden = true) String authenticatedUserId);

    @Operation(
            summary = "쪽지 삭제",
            description = "작성자 본인의 active 쪽지를 soft delete 합니다.",
            operationId = "deleteNote"
    )
    ApiResponse<Void> delete(Long id, @Parameter(hidden = true) String authenticatedUserId);

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
                            schema = @Schema(implementation = NotesResponse.class)
                    )
            )
    })
    ApiResponse<NotesResponse> nearby(@ParameterObject NearbySectionRequest request, @Parameter(hidden = true) String authenticatedUserId);
}
