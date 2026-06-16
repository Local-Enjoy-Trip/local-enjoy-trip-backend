package com.ssafy.enjoytrip.dto.query;

public record NearbyNotesCondition(
        double longitude,
        double latitude,
        double radiusMeters,
        int limit
) {
}
