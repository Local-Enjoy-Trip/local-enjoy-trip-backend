package com.ssafy.enjoytrip.storage.db.core.model;

public record NewsItemRecord(
        String id,
        String title,
        String link,
        String summary,
        String source,
        String publishedAt
) {
}
