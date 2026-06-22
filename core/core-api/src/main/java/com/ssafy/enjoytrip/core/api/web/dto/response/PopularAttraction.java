package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Attraction;

public record PopularAttraction(
        Attraction attraction,
        double distanceMeters,
        long popularityCount
) {
}
