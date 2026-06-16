package com.ssafy.enjoytrip.application.dto.query;

public record NearbySearchCondition(
        double longitude,
        double latitude,
        double radiusMeters,
        int limit
) {
}
