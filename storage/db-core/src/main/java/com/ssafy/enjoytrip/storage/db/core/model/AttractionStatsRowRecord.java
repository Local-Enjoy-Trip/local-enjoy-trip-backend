package com.ssafy.enjoytrip.storage.db.core.model;

public record AttractionStatsRowRecord(
        Long attractionId,
        int saveCount,
        double averageRating,
        int ratingCount,
        boolean saved,
        Integer myRating
) {
}
