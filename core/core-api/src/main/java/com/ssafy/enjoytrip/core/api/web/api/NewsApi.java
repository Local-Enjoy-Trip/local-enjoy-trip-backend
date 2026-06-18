package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NewsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "News", description = "여행 뉴스 API")
public interface NewsApi {

    @Operation(summary = "여행 뉴스 조회", description = "외부 뉴스 소스에서 여행 관련 뉴스를 조회합니다.", operationId = "findNews")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "뉴스 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NewsResponse.class),
                            examples = @ExampleObject(value = """
                                    {"success":true,"data":{"news":[{"id":"n1","title":"봄 여행지 추천","link":"https://example.com/news/1","summary":"요약","source":"example","publishedAt":"2026-05-20"}]},"error":null}
                                    """))
            )
    })
    ApiResponse<NewsResponse> findNews();
}
