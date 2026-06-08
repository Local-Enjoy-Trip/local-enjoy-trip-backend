package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.response.RouteOptimizeResponse;
import com.ssafy.enjoytrip.web.dto.response.RouteSplitByDayResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Route", description = "여행 경로 최적화 API")
public interface RouteApi {

    @Operation(
            summary = "경로 방문 순서 최적화",
            description = """
                    `points` 쿼리 문자열을 기반으로 가까운 순서의 방문 경로를 계산합니다.

                    형식: `lat,lng|lat,lng|lat,lng`
                    예: `37.5665,126.9780|37.5796,126.9770|37.5700,126.9920`
                    """,
            operationId = "optimizeRoute"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "경로 최적화 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteOptimizeResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"data\":{\"order\":[0,1,2],\"totalDistanceKm\":4.21},\"error\":null}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "points 형식 오류")
    })
    ApiResponse<RouteOptimizeResponse> optimize(
            @Parameter(description = "위도/경도 목록. `lat,lng|lat,lng` 형식", example = "37.5665,126.9780|37.5796,126.9770") String points
    );

    @Operation(
            summary = "일자별 경로 분할",
            description = """
                    `points` 목록을 `days` 일수에 맞게 큰 거리 간격 기준으로 분할합니다.

                    `days`가 비어 있거나 숫자가 아니면 1일로 처리됩니다.
                    """,
            operationId = "splitRouteByDay"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "일자별 분할 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteSplitByDayResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"data\":{\"days\":[[0,1],[2,3]],\"dayDistanceKm\":[3.2,5.7]},\"error\":null}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "points 형식 오류")
    })
    ApiResponse<RouteSplitByDayResponse> splitByDay(
            @Parameter(description = "위도/경도 목록. `lat,lng|lat,lng` 형식", example = "37.5665,126.9780|37.5796,126.9770|37.5700,126.9920") String points,
            @Parameter(description = "분할할 여행 일수", example = "2") String days
    );
}
