package com.ssafy.enjoytrip.domain;

import java.util.List;

public record AttractionStats(
        Long attractionId,
        int favoriteCount,
        double ratingAverage,
        int ratingCount,
        List<AttractionTag> tags,
        boolean favorited,
        Integer myRating
) {
}
