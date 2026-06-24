package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.MapSearchTarget;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MapSearchRequest(
        @Schema(description = "검색 키워드", example = "경복궁")
        @NotBlank
        @Size(max = 50)
        String keyword,

        @Schema(description = "지도 중심 경도", example = "126.9780")
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double mapX,

        @Schema(description = "지도 중심 위도", example = "37.5665")
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double mapY,

        @Schema(description = "검색 반경(m), null이면 전역 검색", example = "500")
        @Positive Double radius,

        @Schema(description = "검색 대상 (PLACE, NOTE, ALL), 기본값 ALL", example = "ALL")
        MapSearchTarget target,

        @Schema(description = "쪽지 카테고리 필터", example = "DAILY")
        NoteCategory noteCategory,

        @Schema(description = "최대 검색 개수 (1~50), 기본값 50", example = "50")
        @Positive Integer limit
) {
    private static final String INVALID_COORDINATES_MESSAGE = "위도 또는 경도가 유효하지 않습니다.";
    private static final String INVALID_KEYWORD_MESSAGE = "검색 키워드가 유효하지 않습니다.";

    public String requiredKeyword() {
        if (keyword == null || keyword.isBlank()) {
            throw new ClientInputException(INVALID_KEYWORD_MESSAGE);
        }
        return keyword.trim();
    }

    public double requiredLongitude() {
        if (mapX == null) {
            throw new ClientInputException(INVALID_COORDINATES_MESSAGE);
        }
        return mapX;
    }

    public double requiredLatitude() {
        if (mapY == null) {
            throw new ClientInputException(INVALID_COORDINATES_MESSAGE);
        }
        return mapY;
    }

    public MapSearchTarget normalizedTarget() {
        return target == null ? MapSearchTarget.ALL : target;
    }

    public int cappedLimit() {
        if (limit == null) {
            return 50;
        }
        return Math.min(limit, 50);
    }
}
