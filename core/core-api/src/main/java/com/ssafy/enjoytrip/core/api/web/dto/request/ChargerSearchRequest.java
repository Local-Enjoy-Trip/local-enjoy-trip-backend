package com.ssafy.enjoytrip.core.api.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ChargerSearchRequest(
        @Schema(description = "환경부 지역 코드", example = "11")
        String zcode,

        @Schema(description = "충전소명 또는 주소 검색어", example = "서울")
        String keyword,

        @Schema(description = "페이지 번호", example = "1")
        @Min(1) Integer pageNo,

        @Schema(description = "한 페이지 크기", example = "150")
        @Min(1) @Max(500) Integer numOfRows
) {
    public String normalizedZcode() {
        return trimToNull(zcode);
    }

    public String normalizedKeyword() {
        return trimToNull(keyword);
    }

    public int pageNoOrDefault() {
        if (pageNo == null) {
            return 1;
        }
        return pageNo;
    }

    public int numOfRowsOrDefault() {
        if (numOfRows == null) {
            return 150;
        }
        return numOfRows;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
