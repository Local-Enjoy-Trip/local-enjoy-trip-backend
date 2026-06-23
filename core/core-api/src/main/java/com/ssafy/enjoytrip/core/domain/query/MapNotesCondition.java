package com.ssafy.enjoytrip.core.domain.query;

import com.ssafy.enjoytrip.core.domain.NoteCategory;

public record MapNotesCondition(
        double longitude,
        double latitude,
        double radiusMeters,
        Integer limit,
        Long viewerMemberId,
        NoteCategory category,
        boolean friendOnly
) {
}
