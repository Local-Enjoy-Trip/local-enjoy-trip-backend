package com.ssafy.enjoytrip.core.api.web.dto.response;

public record NewsItem(
        String id,
        String title,
        String link,
        String summary,
        String source,
        String publishedAt
) {
}
