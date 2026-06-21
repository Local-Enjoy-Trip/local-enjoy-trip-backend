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
    private static final String INVALID_REQUEST_MESSAGE = "유효하지 않은 요청입니다.";

    public Double normalizedLongitude() {
        requireCoordinatePair();
        return mapX;
    }

    public Double normalizedLatitude() {
        requireCoordinatePair();
        return mapY;
    }

    public double normalizedRadiusMeters() {
        if (radius != null && (radius <= 0 || radius > 5000)) {
            throw new ClientInputException(INVALID_REQUEST_MESSAGE);
        }

        return radius == null ? DEFAULT_RADIUS_METERS : radius;
    }

    public int normalizedLimit() {
        if (limit != null && (limit <= 0 || limit > 100)) {
            throw new ClientInputException(INVALID_REQUEST_MESSAGE);
        }

        return limit == null ? DEFAULT_LIMIT : limit;
    }

    public MapExploreFilter normalizedFilter() {
        return filter == null ? MapExploreFilter.ALL : filter;
    }

    private void requireCoordinatePair() {
        if ((mapX == null) != (mapY == null)) {
            throw new ClientInputException(INVALID_COORDINATES_MESSAGE);
        }
    }
}
