package com.ssafy.enjoytrip.storage.db.core.mybatis.row;

public record AttractionAverageRatingRow(
        Long attractionId,
        double average,
        int count
) {
}
