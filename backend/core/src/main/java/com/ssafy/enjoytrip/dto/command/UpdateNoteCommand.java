package com.ssafy.enjoytrip.dto.command;

import com.ssafy.enjoytrip.domain.NoteCategory;
import com.ssafy.enjoytrip.domain.NoteImageReference;
import com.ssafy.enjoytrip.domain.NoteVisibility;

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
