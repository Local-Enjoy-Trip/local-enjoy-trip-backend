package com.ssafy.enjoytrip.domain.embedding;

public record AttractionEmbeddingBackfillReport(
        int selectedCount,
        int embeddedCount,
        int skippedCount,
        int failedCount,
        boolean dryRun
) {
}
