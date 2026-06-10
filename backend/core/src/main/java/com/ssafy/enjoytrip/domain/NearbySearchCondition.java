package com.ssafy.enjoytrip.domain;

public record NearbySearchCondition(
        double longitude,
        double latitude,
        double radiusMeters,
        int limit
) {
}
