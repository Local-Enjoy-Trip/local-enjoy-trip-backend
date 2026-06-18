package com.ssafy.enjoytrip.core.domain;

public record PopularAttraction(
        Attraction attraction,
        double distanceMeters,
        long popularityCount
) {
}
