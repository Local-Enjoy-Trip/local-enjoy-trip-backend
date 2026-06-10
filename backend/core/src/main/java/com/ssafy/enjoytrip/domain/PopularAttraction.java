package com.ssafy.enjoytrip.domain;

public record PopularAttraction(
        Attraction attraction,
        double distanceMeters,
        long popularityCount
) {
}
