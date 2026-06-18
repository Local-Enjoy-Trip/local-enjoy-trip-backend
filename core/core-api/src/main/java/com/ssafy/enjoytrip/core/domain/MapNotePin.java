package com.ssafy.enjoytrip.core.domain;

import java.time.LocalDateTime;

public record MapNotePin(
        Long id,
        String title,
        NoteCategory category,
        NoteVisibility visibility,
        Double latitude,
        Double longitude,
        String regionName,
        String imageUrl,
        String authorUserId,
        String authorNickname,
        String authorProfileImageUrl,
        ViewerRelationship relationshipToViewer,
        LocalDateTime createdAt
) {
}
