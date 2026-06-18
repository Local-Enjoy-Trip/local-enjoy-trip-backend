package com.ssafy.enjoytrip.storage.db.core.mybatis.row;

public record AttractionEmbeddingSourceRow(
        Long attractionId,
        String title,
        String addr1,
        String addr2,
        String overview,
        Integer sidoCode,
        Integer gugunCode
) {
}
