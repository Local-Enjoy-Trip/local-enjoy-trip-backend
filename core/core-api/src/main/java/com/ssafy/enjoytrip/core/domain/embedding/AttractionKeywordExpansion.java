package com.ssafy.enjoytrip.core.domain.embedding;

import java.util.List;

public record AttractionKeywordExpansion(
        List<String> keywords
) {
    public AttractionKeywordExpansion {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("확장 키워드가 필요합니다.");
        }
        keywords = keywords.stream()
                .map(keyword -> keyword == null ? "" : keyword.strip())
                .filter(keyword -> !keyword.isEmpty())
                .distinct()
                .toList();
        if (keywords.isEmpty()) {
            throw new IllegalArgumentException("확장 키워드가 필요합니다.");
        }
    }

    public String embeddingText() {
        return String.join("\n", keywords);
    }
}
