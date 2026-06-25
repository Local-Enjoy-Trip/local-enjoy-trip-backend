package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.request.MapExploreRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MapSearchRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.MapExploreResponse;
import com.ssafy.enjoytrip.core.domain.MapPin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;

@Tag(name = "Map", description = "동네핀 지도 탐색 API")
public interface MapApi {
    @Operation(
            summary = "지도 탐색",
            description = "인증 사용자의 지도 중심 주변 장소와 접근 가능한 쪽지를 조회합니다.",
            operationId = "exploreMap"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "지도 탐색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MapExploreResponse.class),
                            examples = @ExampleObject(value = ApiExamples.MAP_EXPLORE_RESPONSE)
                    )
            )
    })
    ApiResponse<MapExploreResponse> explore(
            @ParameterObject @Valid MapExploreRequest request,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(
            summary = "지도 키워드 검색",
            description = "키워드를 통해 주변 장소(관광지)와 쪽지를 통합 검색합니다. 결과는 정렬 기준(매칭 등급 우선, 거리 차순위)으로 단일 리스트로 반환됩니다.",
            operationId = "searchMap"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "지도 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MapPin.class),
                            examples = @ExampleObject(value = ApiExamples.MAP_SEARCH_RESPONSE)
                    )
            )
    })
    ApiResponse<List<MapPin>> search(
            @ParameterObject @Valid MapSearchRequest request,
            @Parameter(hidden = true) Long memberId
    );
}
