package com.ssafy.enjoytrip.repository.embedding;

import com.ssafy.enjoytrip.domain.embedding.AttractionKeywordExpansion;

public interface AttractionKeywordExpansionGateway {
    AttractionKeywordExpansion expand(String attractionSourceText);
}
