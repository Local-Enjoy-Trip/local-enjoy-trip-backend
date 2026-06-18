package com.ssafy.enjoytrip.core.domain.embedding;

public record AttractionEmbeddingTargetRegion(
        String sidoName,
        String gugunName,
        int sidoCode,
        int gugunCode,
        String provenance
) {
}
