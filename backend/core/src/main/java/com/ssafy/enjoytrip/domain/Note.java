package com.ssafy.enjoytrip.domain;

import java.time.LocalDateTime;

public record Note(
        Long id,
        String authorUserId,
        String title,
        String content,
        NoteCategory category,
        NoteVisibility visibility,
        double latitude,
        double longitude,
        String regionName,
        String imageObjectKey,
        String imageUrl,
        String imageContentType,
        NoteStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}
