package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record MapExploreRequest(
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double mapX,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double mapY,
        @Positive @Max(5000) Double radius,
        @Positive @Max(100) Integer limit,
        MapExploreFilter filter,
        NoteCategory noteCategory
) {
    private static final double DEFAULT_RADIUS_METERS = 500.0;
    private static final int DEFAULT_LIMIT = 50;
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

    public int normalizedLimit() {
        return limit == null ? DEFAULT_LIMIT : limit;
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
