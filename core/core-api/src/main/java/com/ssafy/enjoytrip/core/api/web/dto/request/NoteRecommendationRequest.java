package com.ssafy.enjoytrip.core.api.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record NoteRecommendationRequest(
        @Schema(description = "조회 개수, 기본값 10", example = "10")
        @Positive @Max(50) Integer limit
) {
    private static final int DEFAULT_LIMIT = 10;

    public int resolvedLimit() {
        return limit == null ? DEFAULT_LIMIT : limit;
    }
}
