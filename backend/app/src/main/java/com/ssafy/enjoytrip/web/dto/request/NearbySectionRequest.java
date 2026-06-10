package com.ssafy.enjoytrip.web.dto.request;

import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_LATITUDE_OR_LONGITUDE;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_REQUEST;

import com.ssafy.enjoytrip.domain.NearbySearchCondition;
import com.ssafy.enjoytrip.support.error.CoreException;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record NearbySectionRequest(
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double mapX,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double mapY,
        @Positive @Max(5000) Double radius,
        @Positive @Max(50) Integer limit
) {
    private static final double SEOUL_LONGITUDE = 126.9780;
    private static final double SEOUL_LATITUDE = 37.5665;
    private static final double DEFAULT_RADIUS_METERS = 500.0;
    private static final int DEFAULT_LIMIT = 20;

    public NearbySearchCondition toCondition() {
        if ((mapX == null) != (mapY == null)) {
            throw new CoreException(INVALID_LATITUDE_OR_LONGITUDE);
        }
        double longitude = mapX == null ? SEOUL_LONGITUDE : mapX;
        double latitude = mapY == null ? SEOUL_LATITUDE : mapY;
        double radiusMeters = radius == null ? DEFAULT_RADIUS_METERS : radius;
        int normalizedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (radiusMeters <= 0 || radiusMeters > 5000 || normalizedLimit <= 0 || normalizedLimit > 50) {
            throw new CoreException(INVALID_REQUEST);
        }
        return new NearbySearchCondition(longitude, latitude, radiusMeters, normalizedLimit);
    }
}
