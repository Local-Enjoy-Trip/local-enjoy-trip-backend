package com.ssafy.enjoytrip.core.domain.external.embedding;

import com.ssafy.enjoytrip.core.domain.embedding.AttractionEmbeddingResult;

public interface AttractionEmbeddingGateway {
    AttractionEmbeddingResult embed(String text);
}
