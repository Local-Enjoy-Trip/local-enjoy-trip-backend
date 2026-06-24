package com.ssafy.enjoytrip.core.api.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CourseMdFeedRequest(
        @Schema(description = "현재 위치 경도", example = "126.9780")
        @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double mapX,

        @Schema(description = "현재 위치 위도", example = "37.5665")
        @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double mapY,

        @Schema(description = "조회 개수, 기본값 20", example = "20")
        @Positive @Max(50) Integer limit
) {
    private static final int DEFAULT_LIMIT = 20;

    public int resolvedLimit() {
        return limit == null ? DEFAULT_LIMIT : limit;
    }
}
