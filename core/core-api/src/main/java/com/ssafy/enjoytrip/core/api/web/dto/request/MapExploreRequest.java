package com.ssafy.enjoytrip.core.api.web.dto.request;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_LATITUDE_OR_LONGITUDE;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_REQUEST;

import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.support.error.CoreException;
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

    public Double normalizedLongitude() {
        if ((mapX == null) != (mapY == null)) {
            throw new CoreException(INVALID_LATITUDE_OR_LONGITUDE);
        }

        return mapX;
    }

    public Double normalizedLatitude() {
        if ((mapX == null) != (mapY == null)) {
            throw new CoreException(INVALID_LATITUDE_OR_LONGITUDE);
        }

        return mapY;
    }

    public double normalizedRadiusMeters() {
        if (radius != null && (radius <= 0 || radius > 5000)) {
            throw new CoreException(INVALID_REQUEST);
        }

        return radius == null ? DEFAULT_RADIUS_METERS : radius;
    }

    public int normalizedLimit() {
        if (limit != null && (limit <= 0 || limit > 100)) {
            throw new CoreException(INVALID_REQUEST);
        }

        return limit == null ? DEFAULT_LIMIT : limit;
    }

    public MapExploreFilter normalizedFilter() {
        return filter == null ? MapExploreFilter.ALL : filter;
    }
}
