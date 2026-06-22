package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

public record MapExploreRequest(
        @Schema(description = "지도 중심 경도", example = "126.9780")
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double mapX,

        @Schema(description = "지도 중심 위도", example = "37.5665")
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double mapY,

        @Schema(description = "탐색 반경(m), 기본값 500", example = "500")
        @Positive Double radius,

        @Schema(description = "지도 탐색 필터", example = "ALL")
        MapExploreFilter filter,

        @Schema(description = "쪽지 카테고리 필터", example = "DAILY")
        NoteCategory noteCategory
) {
    private static final double DEFAULT_RADIUS_METERS = 500.0;
    private static final String INVALID_COORDINATES_MESSAGE = "위도 또는 경도가 유효하지 않습니다.";

    public double requiredLongitude() {
        requireCoordinates();
        return mapX;
    }

    public double requiredLatitude() {
        requireCoordinates();
        return mapY;
    }

    public double normalizedRadiusMeters() {
        return radius == null ? DEFAULT_RADIUS_METERS : radius;
    }

    public MapExploreFilter normalizedFilter() {
        return filter == null ? MapExploreFilter.ALL : filter;
    }

    private void requireCoordinates() {
        if (mapX == null || mapY == null) {
            throw new ClientInputException(INVALID_COORDINATES_MESSAGE);
        }
    }
}
