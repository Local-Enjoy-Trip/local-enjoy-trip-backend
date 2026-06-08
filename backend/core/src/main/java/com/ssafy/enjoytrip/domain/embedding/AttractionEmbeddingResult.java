package com.ssafy.enjoytrip.domain.embedding;

import java.util.List;

public record AttractionEmbeddingResult(
        String provider,
        String model,
        int dimension,
        List<Double> embedding
) {
    public AttractionEmbeddingResult {
        embedding = List.copyOf(embedding);
    }
}
