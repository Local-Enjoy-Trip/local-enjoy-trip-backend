package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.request.NeighborhoodBriefingRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.NeighborhoodBriefingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Neighborhood", description = "동네핀 홈 AI 브리핑 API")
public interface NeighborhoodBriefingApi {

    @Operation(
            summary = "홈 동네 AI 브리핑 조회",
            description = """
                    홈 상단에 표시할 지역/날씨/저장된 공개 코스 기반 자연어 브리핑을 조회합니다.

                    - 기존 `GET /api/weather/briefings` 응답은 변경하지 않습니다.
                    - 응답은 자연어 `briefing` 중심이며 courseId, 추천 ID 목록, recommendations 배열을 반환하지 않습니다.
                    - GMS/Spring AI 호출이 실패하거나 저장된 코스 후보가 없어도 API는 성공 envelope와 fallback 문장을 반환합니다.
                    """,
            operationId = "getNeighborhoodBriefing"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "홈 동네 AI 브리핑 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NeighborhoodBriefingResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "region": "서울",
                                        "briefing": "오늘 서울은 맑고 더운 편이라 한강 저녁 산책 코스 어떠세요?"
                                      },
                                      "error": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<NeighborhoodBriefingResponse> brief(@ParameterObject NeighborhoodBriefingRequest request);
}
