package com.ssafy.enjoytrip.domain;

public record CreateNoteCommand(
        String authorUserId,
        String title,
        String content,
        NoteCategory category,
        NoteVisibility visibility,
        double latitude,
        double longitude,
        String regionName
) {
}
