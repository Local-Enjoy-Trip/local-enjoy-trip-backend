package com.ssafy.enjoytrip.domain;

public record NewsItem(
        String id,
        String title,
        String link,
        String summary,
        String source,
        String publishedAt
) {
}
