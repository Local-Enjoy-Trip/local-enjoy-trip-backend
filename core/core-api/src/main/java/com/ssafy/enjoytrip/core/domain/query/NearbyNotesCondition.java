package com.ssafy.enjoytrip.core.domain.query;

public record NearbyNotesCondition(
        double longitude,
        double latitude,
        double radiusMeters,
        int limit
) {
}
