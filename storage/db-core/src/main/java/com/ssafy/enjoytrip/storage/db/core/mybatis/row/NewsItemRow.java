package com.ssafy.enjoytrip.storage.db.core.mybatis.row;

public record NewsItemRow(
        String id,
        String title,
        String link,
        String summary,
        String source,
        String publishedAt
) {
}
