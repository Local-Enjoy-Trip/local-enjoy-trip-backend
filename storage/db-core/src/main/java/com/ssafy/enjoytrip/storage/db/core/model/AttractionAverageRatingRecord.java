package com.ssafy.enjoytrip.storage.db.core.model;

public record AttractionAverageRatingRecord(
        Long attractionId,
        double average,
        int count
) {
}
