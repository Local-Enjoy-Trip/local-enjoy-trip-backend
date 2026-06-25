package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteStatus;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import java.time.LocalDateTime;
import java.util.List;

public record NoteResponse(
        Long id,
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
        LocalDateTime updatedAt,
        List<TagInfo> tags
) {
    public record TagInfo(Long id, String name) {}

    public NoteResponse(Note note) {
        this(
                note.id(),
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
                note.updatedAt(),
                List.of()
        );
    }
}
