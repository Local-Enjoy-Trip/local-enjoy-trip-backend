package com.ssafy.enjoytrip.domain.embedding;

public record AttractionEmbeddingSource(
        Long attractionId,
        String title,
        String addr1,
        String addr2,
        String overview,
        Integer sidoCode,
        Integer gugunCode
) {
}
