package com.ssafy.enjoytrip.domain;

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
