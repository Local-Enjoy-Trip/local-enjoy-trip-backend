package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.ChargerSearchRequest;
import com.ssafy.enjoytrip.web.dto.response.ChargersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Chargers", description = "전기차 충전소 검색 API")
public interface ChargerApi {

    @Operation(
            summary = "전기차 충전소 검색",
            description = """
                    환경부 전기차 충전소 데이터를 조회합니다.

                    - `zcode`: 지역 코드
                    - `keyword`: 충전소명/주소 검색어
                    - `pageNo`: 페이지 번호, 기본값 1
                    - `numOfRows`: 한 페이지 크기, 기본값 150
                    """,
            operationId = "findChargers"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "충전소 검색 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChargersResponse.class),
                            examples = @ExampleObject(value = """
                                    {"success":true,"data":{"chargers":[{"statId":"ME000001","statNm":"서울 충전소","chgerId":"01","addr":"서울특별시","lat":37.5665,"lng":126.9780,"stat":"2"}]},"error":null}
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "EV charger API 호출 실패")
    })
    ApiResponse<ChargersResponse> find(@ParameterObject ChargerSearchRequest request);
}
