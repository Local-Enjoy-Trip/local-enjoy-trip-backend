package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Attraction;

public record PopularAttractionResult(
        Attraction attraction,
        double distanceMeters,
        long popularityCount
) {
}
