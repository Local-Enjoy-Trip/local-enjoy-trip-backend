package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Note;
import java.util.List;

public record NotesResponse(List<NoteResponse> notes) {
    public NotesResponse {
        notes = List.copyOf(notes);
    }

    public static NotesResponse from(List<Note> notes) {
        return new NotesResponse(notes.stream()
                .map(NoteResponse::new)
                .toList());
    }
}
