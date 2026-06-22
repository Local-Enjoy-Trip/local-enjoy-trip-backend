package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
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
