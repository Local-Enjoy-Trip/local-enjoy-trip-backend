package com.ssafy.enjoytrip.core.domain;

public record BoardPost(
        String id,
        String title,
        String content,
        String author,
        String createdAt,
        String updatedAt
) {
}
