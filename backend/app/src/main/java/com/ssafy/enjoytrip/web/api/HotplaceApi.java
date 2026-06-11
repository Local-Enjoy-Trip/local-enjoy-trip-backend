package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.HotplaceCreateRequest;
import com.ssafy.enjoytrip.web.dto.response.HotplacesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Hotplaces", description = "사용자 핫플레이스 API")
public interface HotplaceApi {

    @Operation(summary = "핫플레이스 목록 조회", description = "`userId`가 있으면 해당 사용자의 핫플레이스만, 없으면 전체 핫플레이스를 조회합니다.", operationId = "findHotplaces")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "핫플레이스 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HotplacesResponse.class),
                            examples = @ExampleObject(value = """
                                    {"success":true,"data":{"hotplaces":[{"id":"h1","userId":"ssafy","title":"성산일출봉","type":"nature","visitDate":"2026-05-20","lat":33.458,"lng":126.942,"description":"일출 명소","photo":"","createdAt":"2026-05-20"}]},"error":null}
                                    """))
            )
    })
    ApiResponse<HotplacesResponse> find(@Parameter(description = "사용자 ID 필터", example = "ssafy") String userId);

    @Operation(summary = "핫플레이스 생성", description = "JSON 본문에 `id`, `userId`, `title`, `type`, `visitDate`, 숫자형 `lat`, `lng`가 필요합니다.", operationId = "createHotplace")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "핫플레이스 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 본문")
    })
    ResponseEntity<ApiResponse<Void>> create(HotplaceCreateRequest request);

    @Operation(summary = "핫플레이스 삭제", description = "경로의 `id` 핫플레이스를 삭제합니다.", operationId = "deleteHotplace")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "핫플레이스 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "id 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "핫플레이스 없음")
    })
    ApiResponse<Void> delete(@Parameter(description = "삭제할 핫플레이스 ID", example = "h1", required = true) String id);
}
