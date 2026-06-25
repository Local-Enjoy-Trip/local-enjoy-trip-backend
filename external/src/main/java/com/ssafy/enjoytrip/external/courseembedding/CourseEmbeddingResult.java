package com.ssafy.enjoytrip.external.courseembedding;

import java.util.List;

public record CourseEmbeddingResult(
        String description,
        String provider,
        String model,
        int dimension,
        List<Double> embedding
) {
    public CourseEmbeddingResult {
        embedding = List.copyOf(embedding);
    }
}
