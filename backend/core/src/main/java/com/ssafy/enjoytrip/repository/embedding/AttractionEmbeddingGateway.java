package com.ssafy.enjoytrip.repository.embedding;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingResult;

public interface AttractionEmbeddingGateway {
    AttractionEmbeddingResult embed(String sourceText);
}
