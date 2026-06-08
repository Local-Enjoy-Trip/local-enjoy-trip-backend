package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.AttractionTagsRequest;
import com.ssafy.enjoytrip.web.dto.request.AttractionSearchRequest;
import com.ssafy.enjoytrip.web.dto.request.RatingRequest;
import com.ssafy.enjoytrip.web.dto.response.AttractionStatsResponse;
import com.ssafy.enjoytrip.web.dto.response.AttractionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.oauth2.jwt.Jwt;

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
                                    {"success":true,"data":{"attractions":[{"id":125405,"title":"경복궁","addr1":"서울 종로구 사직로 161","latitude":37.579617,"longitude":126.977041,"contentTypeId":"12"}]},"error":null}
                                    """)
                            )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Tour API 호출 실패")
    })
    ApiResponse<AttractionsResponse> search(@ParameterObject AttractionSearchRequest request, Jwt jwt);

    @Operation(
            summary = "관광지 POST 차단",
            description = "레거시 클라이언트 오동작을 명확히 알리기 위해 POST /api/attractions 요청은 405로 거절합니다.",
            operationId = "rejectAttractionsPost"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "405", description = "GET /api/attractions 사용 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ApiResponse<Void> rejectPost();

    @Operation(summary = "관광지 찜", description = "인증 사용자의 관광지 찜을 저장합니다.", operationId = "favoriteAttraction")
    ApiResponse<Void> favorite(Long id, Jwt jwt);

    @Operation(summary = "관광지 찜 해제", description = "인증 사용자의 관광지 찜을 삭제합니다.", operationId = "unfavoriteAttraction")
    ApiResponse<Void> unfavorite(Long id, Jwt jwt);

    @Operation(summary = "관광지 평점 등록", description = "인증 사용자의 1~5 평점을 등록하거나 갱신합니다.", operationId = "rateAttraction")
    ApiResponse<Void> rate(Long id, @ParameterObject RatingRequest request, Jwt jwt);

    @Operation(summary = "관광지 평점 삭제", description = "인증 사용자의 평점을 삭제합니다.", operationId = "deleteAttractionRating")
    ApiResponse<Void> deleteRating(Long id, Jwt jwt);

    @Operation(summary = "관광지 통계 조회", description = "찜 수, 평균 평점, 태그와 내 사용자 상태를 조회합니다.", operationId = "getAttractionStats")
    ApiResponse<AttractionStatsResponse> stats(Long id, Jwt jwt);

    @Operation(summary = "관광지 태그 연결", description = "관광지에 연결된 태그 목록을 교체합니다.", operationId = "replaceAttractionTags")
    ApiResponse<Void> replaceTags(Long id, @ParameterObject AttractionTagsRequest request, Jwt jwt);
}
