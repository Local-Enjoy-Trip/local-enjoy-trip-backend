package com.ssafy.enjoytrip.domain.embedding;

import java.util.List;

public record AttractionKeywordExpansion(
        List<String> keywords
) {
    public AttractionKeywordExpansion {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("expanded keywords are required");
        }
        keywords = keywords.stream()
                .map(keyword -> keyword == null ? "" : keyword.strip())
                .filter(keyword -> !keyword.isEmpty())
                .distinct()
                .toList();
        if (keywords.isEmpty()) {
            throw new IllegalArgumentException("expanded keywords are required");
        }
    }

    public String embeddingText() {
        return String.join("\n", keywords);
    }
}
