package com.ssafy.enjoytrip.core.domain;

import java.time.LocalDateTime;

public record NoteMapPin(
        Long id,
        String title,
        NoteCategory category,
        NoteVisibility visibility,
        Double latitude,
        Double longitude,
        String regionName,
        double distanceMeters,
        String imageObjectKey,
        String authorUserId,
        String authorNickname,
        String authorProfileImageUrl,
        NoteViewerRelationship relationshipToViewer,
        LocalDateTime createdAt
) {
}
