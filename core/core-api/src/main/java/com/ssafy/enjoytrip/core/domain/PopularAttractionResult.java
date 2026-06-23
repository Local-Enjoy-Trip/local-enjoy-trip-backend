package com.ssafy.enjoytrip.core.domain;


public record PopularAttractionResult(
        Attraction attraction,
        double distanceMeters,
        long popularityCount
) {
}
