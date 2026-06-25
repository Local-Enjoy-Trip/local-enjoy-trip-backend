package com.ssafy.enjoytrip.core.api.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.util.StringUtils;

public record CourseRecommendationRequest(
        @Schema(description = "폴백 기준 동네명", example = "역삼동")
        @NotBlank String regionName,

        @Schema(description = "조회 개수, 기본값 10", example = "10")
        @Positive @Max(50) Integer limit
) {
    private static final int DEFAULT_LIMIT = 10;

    public String normalizedRegionName() {
        return StringUtils.hasText(regionName) ? regionName.strip() : "";
    }

    public int resolvedLimit() {
        return limit == null ? DEFAULT_LIMIT : limit;
    }
}
