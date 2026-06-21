package com.ssafy.enjoytrip.storage.db.core.model;

public record AttractionStatsRowRecord(
        Long attractionId,
        int favoriteCount,
        int saveCount,
        double averageRating,
        int ratingCount,
        Long tagId,
        String tagName,
        boolean favorited,
        boolean saved,
        Integer myRating
) {
}
