package com.ssafy.enjoytrip.core.domain.query;

import com.ssafy.enjoytrip.core.domain.NoteCategory;

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
