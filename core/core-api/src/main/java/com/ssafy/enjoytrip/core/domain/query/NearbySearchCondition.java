package com.ssafy.enjoytrip.core.domain.query;

public record NearbySearchCondition(
        double longitude,
        double latitude,
        double radiusMeters,
        Integer limit
) {
}
