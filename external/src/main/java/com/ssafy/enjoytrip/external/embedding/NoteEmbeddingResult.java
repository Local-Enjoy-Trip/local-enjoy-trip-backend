package com.ssafy.enjoytrip.external.embedding;

import java.util.List;

public record NoteEmbeddingResult(
        String provider,
        String model,
        int dimension,
        List<Double> embedding
) {
    public NoteEmbeddingResult {
        embedding = List.copyOf(embedding);
    }
}
