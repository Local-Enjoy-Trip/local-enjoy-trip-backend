package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.MapExploreRequest;
import com.ssafy.enjoytrip.web.dto.response.MapExploreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Map", description = "동네핀 지도 탐색 API")
public interface MapApi {
    @Operation(
            summary = "지도 탐색",
            description = "인증 사용자의 지도 중심 주변 장소와 접근 가능한 쪽지를 조회합니다.",
            operationId = "exploreMap"
    )
    ApiResponse<MapExploreResponse> explore(@ParameterObject MapExploreRequest request, @Parameter(hidden = true) String authenticatedUserId);
}
