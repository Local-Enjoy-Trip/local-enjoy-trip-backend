package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.PlanCreateRequest;
import com.ssafy.enjoytrip.web.dto.request.PlanReplaceItemsRequest;
import com.ssafy.enjoytrip.web.dto.request.PlanUpdateRequest;
import com.ssafy.enjoytrip.web.dto.response.PlanResponse;
import com.ssafy.enjoytrip.web.dto.response.PlansResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Plans", description = "여행 계획 API")
public interface PlanApi {

    @Operation(
            summary = "여행 계획 목록 조회",
            description = "`userId`가 있으면 해당 사용자의 계획만, 없으면 전체 계획을 조회합니다.",
            operationId = "findPlans"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "여행 계획 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlansResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "plans": [
                                          {
                                            "id": "p1",
                                            "userId": "ssafy",
                                            "title": "제주 여행",
                                            "startDate": "2026-05-20",
                                            "endDate": "2026-05-22",
                                            "budget": 300000,
                                            "note": "렌터카",
                                            "routeItems": [],
                                            "createdAt": "2026-05-20"
                                          }
                                        ]
                                      },
                                      "error": null
                                    }
                                    """))
            )
    })
    ApiResponse<PlansResponse> find(@Parameter(description = "사용자 ID 필터", example = "ssafy") String userId);

    @Operation(
            summary = "여행 계획 단건 조회",
            description = "경로의 `id` 여행 계획을 조회합니다.",
            operationId = "findPlan"
    )
    ApiResponse<PlanResponse> findOne(String id, Jwt jwt);

    @Operation(
            summary = "여행 계획 생성",
            description = "JSON request body로 여행 계획과 코스 항목을 생성합니다. 사용자 ID는 JWT subject를 사용합니다.",
            operationId = "createPlan"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "여행 계획 생성 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "필수 필드 또는 범위 validation 실패"
            )
    })
    ApiResponse<Void> create(PlanCreateRequest request, Jwt jwt);

    @Operation(
            summary = "여행 계획 수정",
            description = "인증 사용자의 여행 계획 메타데이터와 코스를 JSON으로 수정합니다.",
            operationId = "updatePlan"
    )
    ApiResponse<Void> update(String id, PlanUpdateRequest request, Jwt jwt);

    @Operation(
            summary = "여행 계획 코스 교체",
            description = "여행 계획의 코스 항목을 JSON 배열 순서대로 교체합니다.",
            operationId = "replacePlanItems"
    )
    ApiResponse<Void> replaceItems(String id, PlanReplaceItemsRequest request, Jwt jwt);

    @Operation(
            summary = "여행 계획 코스 항목 삭제",
            description = "여행 계획의 코스 항목 하나를 삭제하고 순서를 재정렬합니다.",
            operationId = "deletePlanItem"
    )
    ApiResponse<Void> deleteItem(String id, Long itemId, Jwt jwt);

    @Operation(
            summary = "여행 계획 삭제",
            description = "경로의 `id` 여행 계획을 삭제합니다.",
            operationId = "deletePlan"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "여행 계획 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "id 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "여행 계획 없음"
            )
    })
    ApiResponse<Void> delete(
            @Parameter(description = "삭제할 여행 계획 ID", example = "p1", required = true) String id,
            Jwt jwt
    );
}
