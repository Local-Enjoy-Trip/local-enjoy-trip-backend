package com.ssafy.enjoytrip.core.domain.query;

public record DistanceSearchCondition(
        double longitude,
        double latitude,
        Integer limit,
        Double radiusMeters
) {
}
