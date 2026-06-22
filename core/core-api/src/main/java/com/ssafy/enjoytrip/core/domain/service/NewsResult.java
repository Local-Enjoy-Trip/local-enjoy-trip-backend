package com.ssafy.enjoytrip.core.domain.service;

public record NewsResult(
        String id,
        String title,
        String link,
        String summary,
        String source,
        String publishedAt
) {
}
