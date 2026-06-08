package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.PlanItemsRequest;
import com.ssafy.enjoytrip.web.dto.request.PlanRequest;
import com.ssafy.enjoytrip.web.dto.response.PlanResponse;
import com.ssafy.enjoytrip.web.dto.response.PlansResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Plans", description = "여행 계획 API")
public interface PlanApi {

    @Operation(summary = "여행 계획 목록 조회", description = "`userId`가 있으면 해당 사용자의 계획만, 없으면 전체 계획을 조회합니다.", operationId = "findPlans")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "여행 계획 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlansResponse.class),
                            examples = @ExampleObject(value = """
                                    {"success":true,"data":{"plans":[{"id":"p1","userId":"ssafy","title":"제주 여행","startDate":"2026-05-20","endDate":"2026-05-22","budget":300000,"note":"렌터카","routeItems":[],"createdAt":"2026-05-20"}]},"error":null}
                                    """))
            )
    })
    ApiResponse<PlansResponse> find(@Parameter(description = "사용자 ID 필터", example = "ssafy") String userId);

    @Operation(summary = "여행 계획 단건 조회", description = "경로의 `id` 여행 계획을 조회합니다.", operationId = "findPlan")
    ApiResponse<PlanResponse> findOne(String id, Jwt jwt);

    @Operation(
            summary = "여행 계획 레거시 액션 처리",
            description = "`action=create`는 생성, `action=delete`는 삭제로 위임됩니다. 신규 클라이언트는 `/items`, `/{id}` 사용을 권장합니다.",
            operationId = "legacyPlanPost"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "액션 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 action, id 누락 또는 필수 필드 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여행 계획 없음")
    })
    ApiResponse<Void> legacyPost(@ParameterObject PlanRequest request, Jwt jwt);

    @Operation(
            summary = "여행 계획 생성",
            description = """
                    여행 계획을 생성합니다.

                    `id`, `userId`, `title`, `startDate`, `endDate`가 필수이며 `budget`은 숫자가 아니면 0으로 처리됩니다.
                    `routeItems`는 JSON 배열 문자열을 권장하며 비어 있으면 `[]`로 저장됩니다.
                    """,
            operationId = "createPlan"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "여행 계획 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 필드 누락")
    })
    ApiResponse<Void> create(@ParameterObject PlanRequest request, Jwt jwt);

    @Operation(summary = "여행 계획 수정", description = "인증 사용자의 여행 계획 메타데이터와 코스를 수정합니다.", operationId = "updatePlan")
    ApiResponse<Void> update(String id, @ParameterObject PlanRequest request, Jwt jwt);

    @Operation(summary = "여행 계획 코스 교체", description = "여행 계획의 코스 항목을 전달된 순서대로 교체합니다.", operationId = "replacePlanItems")
    ApiResponse<Void> replaceItems(String id, @ParameterObject PlanItemsRequest request, Jwt jwt);

    @Operation(summary = "여행 계획 코스 항목 삭제", description = "여행 계획의 코스 항목 하나를 삭제하고 순서를 재정렬합니다.", operationId = "deletePlanItem")
    ApiResponse<Void> deleteItem(String id, Long itemId, Jwt jwt);

    @Operation(summary = "여행 계획 삭제", description = "경로의 `id` 여행 계획을 삭제합니다.", operationId = "deletePlan")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "여행 계획 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "id 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여행 계획 없음")
    })
    ApiResponse<Void> delete(@Parameter(description = "삭제할 여행 계획 ID", example = "p1", required = true) String id,
                             Jwt jwt);
}
