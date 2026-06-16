package com.ssafy.enjoytrip.application.dto.query;

public record NearbyNotesCondition(
        double longitude,
        double latitude,
        double radiusMeters,
        int limit
) {
}
