package com.ssafy.enjoytrip.domain;

public record UpdateNoteCommand(
        Long id,
        String authorUserId,
        String title,
        String content,
        NoteCategory category,
        NoteVisibility visibility,
        double latitude,
        double longitude,
        String regionName,
        NoteImageReference imageReference
) {
}
