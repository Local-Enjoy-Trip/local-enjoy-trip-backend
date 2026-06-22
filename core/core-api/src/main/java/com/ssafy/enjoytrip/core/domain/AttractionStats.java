package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public record AttractionStats(
        Long attractionId,
        int saveCount,
        double ratingAverage,
        int ratingCount,
        List<AttractionTag> tags,
        boolean saved,
        Integer myRating
) {
}
