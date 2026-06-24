package com.ssafy.enjoytrip.core.api.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CoursePopularFeedRequest(
        @Schema(description = "동네 이름 (regionName과 일치하는 코스 조회)", example = "서울 종로구")
        @NotBlank String regionName,

        @Schema(description = "조회 개수, 기본값 20", example = "20")
        @Positive @Max(50) Integer limit
) {
    private static final int DEFAULT_LIMIT = 20;

    public int resolvedLimit() {
        return limit == null ? DEFAULT_LIMIT : limit;
    }

    public String normalizedRegionName() {
        return regionName.strip();
    }
}
