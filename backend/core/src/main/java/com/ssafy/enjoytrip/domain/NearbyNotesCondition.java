package com.ssafy.enjoytrip.domain;

public record NearbyNotesCondition(
        double longitude,
        double latitude,
        double radiusMeters,
        int limit
) {
}
