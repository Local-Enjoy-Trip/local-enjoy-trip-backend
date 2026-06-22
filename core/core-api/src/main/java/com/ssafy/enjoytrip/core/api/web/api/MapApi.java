package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.request.MapExploreRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.MapExploreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

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
            @ParameterObject MapExploreRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );
}
