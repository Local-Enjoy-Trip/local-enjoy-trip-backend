package com.ssafy.enjoytrip.storage.db.core.model;

public record AttractionEmbeddingSourceRecord(
        Long attractionId,
        String title,
        String addr1,
        String addr2,
        String overview,
        Integer sidoCode,
        Integer gugunCode
) {
}
