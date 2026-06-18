package com.ssafy.enjoytrip.core.domain.external.embedding;

import com.ssafy.enjoytrip.core.domain.embedding.AttractionKeywordExpansion;

public interface AttractionKeywordExpansionGateway {
    AttractionKeywordExpansion expand(String text);
}
