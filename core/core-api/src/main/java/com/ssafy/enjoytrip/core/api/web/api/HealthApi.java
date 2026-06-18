package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.DbHealthResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health", description = "애플리케이션과 DB 연결 상태 확인 API")
public interface HealthApi {

    @Operation(summary = "애플리케이션 헬스 체크", description = "서버 프로세스가 요청을 처리할 수 있는지 확인합니다.", operationId = "health")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "서버 정상",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"data\":{\"status\":\"ok\"},\"error\":null}"))
            )
    })
    ApiResponse<HealthResponse> health();

    @Operation(summary = "DB 헬스 체크", description = "애플리케이션에서 데이터베이스 연결이 가능한지 확인합니다.", operationId = "dbHealth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "DB 연결 정상",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DbHealthResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"data\":{\"status\":\"ok\",\"db\":\"connected\"},\"error\":null}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "DB 연결 실패")
    })
    ApiResponse<DbHealthResponse> dbHealth();
}
