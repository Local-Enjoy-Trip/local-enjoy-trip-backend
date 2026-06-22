package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.request.AttractionTagsRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.AttractionSearchRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NearbySectionRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.RatingRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.AttractionStatsResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.AttractionsResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.PopularAttractionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Attractions", description = "관광지 검색 API")
public interface AttractionApi {

    @Operation(
            summary = "관광지 검색",
            description = """
                    관광지 목록을 조회합니다.

                    - `mapX`와 `mapY`를 모두 전달하면 현재 좌표 기준 반경 검색을 수행합니다.
                    - 좌표를 전달하지 않으면 `sidoCode`, `gugunCode`, `contentTypeId`, `keyword` 조건으로 일반 검색합니다.
                    - `radius` 기본값은 3000m입니다.
                    """,
            operationId = "searchAttractions"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관광지 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AttractionsResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "attractions": [
                                          {
                                            "id": 125405,
                                            "title": "경복궁",
                                            "addr1": "서울 종로구 사직로 161",
                                            "latitude": 37.579617,
                                            "longitude": 126.977041,
                                            "contentTypeId": "12"
                                          }
                                        ]
                                      },
                                      "error": null
                                    }
                                    """)
                            )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "502",
                    description = "Tour API 호출 실패"
            )
    })
    ApiResponse<AttractionsResponse> search(
            @ParameterObject AttractionSearchRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );

    @Operation(
            summary = "홈 인기 주변 관광지 조회",
            description = """
                    동네핀 홈의 주변 인기 관광지 섹션을 조회합니다.

                    - 좌표를 전달하지 않으면 서울 시청 좌표(`mapX=126.9780`, `mapY=37.5665`)를 사용합니다.
                    - `radius` 기본값은 500m이며 쪽지 주변 조회와 동일한 기본 반경입니다.
                    - 먼저 PostGIS로 반경 안 후보를 찾고, RDB `save_count`를 `popularityCount`로 채운 뒤
                      내림차순, 거리, 제목/ID 순으로 정렬합니다.
                    - 집계 행이 없는 후보의 `popularityCount`는 0으로 반환합니다.
                    - `saveCount`, `saved`는 장소 저장 상태를 제공합니다.
                    """,
            operationId = "getPopularNearbyAttractions"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "홈 인기 주변 관광지 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PopularAttractionsResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "attractions": [
                                          {
                                            "id": 125405,
                                            "title": "경복궁",
                                            "latitude": 37.579617,
                                            "longitude": 126.977041,
                                            "contentTypeId": "12",
                                            "saveCount": 5,
                                            "saved": true,
                                            "popularityCount": 5
                                          }
                                        ]
                                      },
                                      "error": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<PopularAttractionsResponse> popularNearby(
            @ParameterObject NearbySectionRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );

    @Operation(
            summary = "관광지 POST 차단",
            description = "레거시 클라이언트 오동작을 명확히 알리기 위해 POST /api/attractions 요청은 405로 거절합니다.",
            operationId = "rejectAttractionsPost"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "405",
                    description = "GET /api/attractions 사용 필요",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "error": {
                                        "code": "C405",
                                        "message": "GET /api/attractions를 사용하세요."
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    ApiResponse<Void> rejectPost();

    @Operation(summary = "관광지 저장", description = "인증 사용자의 관광지 저장을 추가합니다.", operationId = "saveAttraction")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관광지 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> save(
            @Parameter(description = "저장할 관광지 ID", example = "125405", required = true) Long id,
            @Parameter(hidden = true) String authenticatedUserId
    );

    @Operation(
            summary = "관광지 저장 해제",
            description = "인증 사용자의 관광지 저장을 삭제합니다.",
            operationId = "unsaveAttraction"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관광지 저장 해제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> unsave(
            @Parameter(description = "저장 해제할 관광지 ID", example = "125405", required = true) Long id,
            @Parameter(hidden = true) String authenticatedUserId
    );

    @Operation(
            summary = "관광지 평점 등록",
            description = "인증 사용자의 1~5 평점을 등록하거나 갱신합니다.",
            operationId = "rateAttraction",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RatingRequest.class),
                            examples = @ExampleObject(value = ApiExamples.RATING_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관광지 평점 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> rate(
            @Parameter(description = "평점을 등록할 관광지 ID", example = "125405", required = true) Long id,
            RatingRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );

    @Operation(
            summary = "관광지 평점 삭제",
            description = "인증 사용자의 평점을 삭제합니다.",
            operationId = "deleteAttractionRating"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관광지 평점 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> deleteRating(
            @Parameter(description = "평점을 삭제할 관광지 ID", example = "125405", required = true) Long id,
            @Parameter(hidden = true) String authenticatedUserId
    );

    @Operation(
            summary = "관광지 통계 조회",
            description = "저장 수, 평균 평점, 태그와 내 사용자 상태를 조회합니다.",
            operationId = "getAttractionStats"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관광지 통계 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AttractionStatsResponse.class),
                            examples = @ExampleObject(value = ApiExamples.ATTRACTION_STATS_RESPONSE)
                    )
            )
    })
    ApiResponse<AttractionStatsResponse> stats(
            @Parameter(description = "통계를 조회할 관광지 ID", example = "125405", required = true) Long id,
            @Parameter(hidden = true) String authenticatedUserId
    );

    @Operation(
            summary = "관광지 태그 연결",
            description = "관광지에 연결된 태그 목록을 교체합니다.",
            operationId = "replaceAttractionTags",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AttractionTagsRequest.class),
                            examples = @ExampleObject(value = ApiExamples.ATTRACTION_TAGS_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관광지 태그 연결 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            )
    })
    ApiResponse<Void> replaceTags(
            @Parameter(description = "태그를 교체할 관광지 ID", example = "125405", required = true) Long id,
            AttractionTagsRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );
}
