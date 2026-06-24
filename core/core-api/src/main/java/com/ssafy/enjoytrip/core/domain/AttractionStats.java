package com.ssafy.enjoytrip.core.domain;

public record AttractionStats(
        Long attractionId,
        int saveCount,
        double ratingAverage,
        int ratingCount,
        boolean saved,
        Integer myRating
) {
}
