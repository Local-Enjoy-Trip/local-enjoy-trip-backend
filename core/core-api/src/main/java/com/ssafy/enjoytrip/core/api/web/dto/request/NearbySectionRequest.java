package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.query.NearbyNotesCondition;
import com.ssafy.enjoytrip.core.domain.query.NearbySearchCondition;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record NearbySectionRequest(
        @Schema(description = "현재 위치 경도, 생략 시 서울 시청 기준", example = "126.9780")
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double mapX,

        @Schema(description = "현재 위치 위도, 생략 시 서울 시청 기준", example = "37.5665")
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double mapY,

        @Schema(description = "조회 반경(m), 기본값 500", example = "500")
        @Positive @Max(5000) Double radius,

        @Schema(description = "조회 개수, 기본값 20", example = "20")
        @Positive @Max(50) Integer limit
) {
    private static final double SEOUL_LONGITUDE = 126.9780;
    private static final double SEOUL_LATITUDE = 37.5665;
    private static final double DEFAULT_RADIUS_METERS = 500.0;
    private static final int DEFAULT_LIMIT = 20;
    private static final String INVALID_COORDINATES_MESSAGE = "위도 또는 경도가 유효하지 않습니다.";
    private static final String INVALID_REQUEST_MESSAGE = "유효하지 않은 요청입니다.";

    public NearbySearchCondition toCondition() {
        NormalizedNearbySection normalized = normalize();

        return new NearbySearchCondition(
                normalized.longitude(),
                normalized.latitude(),
                normalized.radiusMeters(),
                normalized.limit()
        );
    }

    public NearbyNotesCondition toNotesCondition() {
        NormalizedNearbySection normalized = normalize();

        return new NearbyNotesCondition(
                normalized.longitude(),
                normalized.latitude(),
                normalized.radiusMeters(),
                normalized.limit()
        );
    }

    private NormalizedNearbySection normalize() {
        if ((mapX == null) != (mapY == null)) {
            throw new ClientInputException(INVALID_COORDINATES_MESSAGE);
        }

        double longitude = mapX == null ? SEOUL_LONGITUDE : mapX;
        double latitude = mapY == null ? SEOUL_LATITUDE : mapY;
        double radiusMeters = radius == null ? DEFAULT_RADIUS_METERS : radius;
        int normalizedLimit = limit == null ? DEFAULT_LIMIT : limit;

        if (radiusMeters <= 0 || radiusMeters > 5000 || normalizedLimit <= 0 || normalizedLimit > 50) {
            throw new ClientInputException(INVALID_REQUEST_MESSAGE);
        }

        return new NormalizedNearbySection(longitude, latitude, radiusMeters, normalizedLimit);
    }

    private record NormalizedNearbySection(
            double longitude,
            double latitude,
            double radiusMeters,
            int limit
    ) {
    }
}
