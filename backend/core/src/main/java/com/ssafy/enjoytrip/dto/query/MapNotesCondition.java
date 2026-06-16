package com.ssafy.enjoytrip.dto.query;

import com.ssafy.enjoytrip.domain.NoteCategory;

public record MapNotesCondition(
        double longitude,
        double latitude,
        double radiusMeters,
        int limit,
        String viewerUserId,
        NoteCategory category,
        boolean friendOnly
) {
}
