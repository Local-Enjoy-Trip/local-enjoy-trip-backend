package com.ssafy.enjoytrip.core.domain;

public record NewsItem(
        String id,
        String title,
        String link,
        String summary,
        String source,
        String publishedAt
) {
}
