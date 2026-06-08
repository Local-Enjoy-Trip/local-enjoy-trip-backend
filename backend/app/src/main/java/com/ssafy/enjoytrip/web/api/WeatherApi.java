package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.response.WeatherBriefingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Weather", description = "기상 브리핑 API")
public interface WeatherApi {

    @Operation(
            summary = "기상 브리핑 조회",
            description = "서울, 부산, 제주 기본 지역의 기상 브리핑을 조회합니다.",
            operationId = "findWeatherBriefings"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "기상 브리핑 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherBriefingsResponse.class),
                            examples = @ExampleObject(value = """
                                    {"success":true,"data":{"weather":[{"region":"서울","condition":"맑음","temperature":22,"rainChance":10,"sunrise":"05:23","sunset":"19:33"}]},"error":null}
                                    """))
            )
    })
    ApiResponse<WeatherBriefingsResponse> findWeatherBriefings();
}
