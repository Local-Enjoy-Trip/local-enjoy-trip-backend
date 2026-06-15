package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Note;
import com.ssafy.enjoytrip.domain.NoteCategory;
import com.ssafy.enjoytrip.domain.NoteStatus;
import com.ssafy.enjoytrip.domain.NoteVisibility;
import java.time.LocalDateTime;

public record NoteResponse(
        Long id,
        String authorUserId,
        String title,
        String content,
        NoteCategory category,
        NoteVisibility visibility,
        Double latitude,
        Double longitude,
        String regionName,
        String imageObjectKey,
        NoteStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public NoteResponse(Note note) {
        this(
                note.id(),
                note.authorUserId(),
                note.title(),
                note.content(),
                note.category(),
                note.visibility(),
                note.latitude(),
                note.longitude(),
                note.regionName(),
                note.imageObjectKey(),
                note.status(),
                note.createdAt(),
                note.updatedAt()
        );
    }
}
