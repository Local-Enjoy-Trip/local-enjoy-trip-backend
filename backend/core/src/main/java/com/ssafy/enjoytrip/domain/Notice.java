package com.ssafy.enjoytrip.domain;

public record Notice(
        Long id,
        String title,
        String content,
        String author,
        String createdAt,
        String updatedAt
) {
}
