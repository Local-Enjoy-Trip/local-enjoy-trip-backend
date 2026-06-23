package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CourseFeedRequest(
        @Schema(description = "현재 위치 경도", example = "126.9780")
        @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double mapX,

        @Schema(description = "현재 위치 위도", example = "37.5665")
        @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double mapY,

        @Schema(description = "조회 개수, 기본값 20", example = "20")
        @Positive @Max(50) Integer limit,

        @Schema(description = "조회 반경(m), 생략 시 반경 제한 없이 거리순 조회", example = "5000")
        @Positive @DecimalMax(value = "20000.0") Double radius
) {
    private static final int DEFAULT_LIMIT = 20;

    public DistanceSearchCondition toCondition() {
        return new DistanceSearchCondition(
                mapX,
                mapY,
                limit == null ? DEFAULT_LIMIT : limit,
                radius
        );
    }
}
