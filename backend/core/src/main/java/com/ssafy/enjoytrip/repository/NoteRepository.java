package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.CreateNoteCommand;
import com.ssafy.enjoytrip.domain.MapNotesCondition;
import com.ssafy.enjoytrip.domain.NearbyNotesCondition;
import com.ssafy.enjoytrip.domain.Note;
import com.ssafy.enjoytrip.domain.NoteMapPin;
import com.ssafy.enjoytrip.domain.UpdateNoteCommand;
import java.util.List;
import java.util.Optional;

public interface NoteRepository {
    Note save(CreateNoteCommand command);

    Optional<Note> findById(Long id);

    Optional<Note> updateOwned(UpdateNoteCommand command);

    boolean softDeleteOwned(Long id, String authorUserId);

    List<Note> findNearbyAccessible(NearbyNotesCondition condition, String viewerUserId);

    List<NoteMapPin> findMapNotes(MapNotesCondition condition);
}
