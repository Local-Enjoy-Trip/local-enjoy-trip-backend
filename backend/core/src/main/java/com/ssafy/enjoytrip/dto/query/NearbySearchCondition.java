package com.ssafy.enjoytrip.dto.query;

public record NearbySearchCondition(
        double longitude,
        double latitude,
        double radiusMeters,
        int limit
) {
}
