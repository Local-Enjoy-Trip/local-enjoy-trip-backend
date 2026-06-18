package com.ssafy.enjoytrip.core.domain.embedding;

public record AttractionEmbeddingBackfillReport(
        int selectedCount,
        int embeddedCount,
        int skippedCount,
        int failedCount,
        boolean dryRun
) {
}
