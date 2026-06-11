package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.NOTE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.NOTE_NOT_ACTIVE;
import static com.ssafy.enjoytrip.support.error.ErrorType.NOTE_NOT_FOUND;

import com.ssafy.enjoytrip.domain.CreateNoteCommand;
import com.ssafy.enjoytrip.domain.NearbyNotesCondition;
import com.ssafy.enjoytrip.domain.Note;
import com.ssafy.enjoytrip.domain.NoteStatus;
import com.ssafy.enjoytrip.domain.UpdateNoteCommand;
import com.ssafy.enjoytrip.repository.NoteRepository;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository repository;

    public Note createNote(CreateNoteCommand command) {
        return repository.save(command);
    }

    public Note updateNote(UpdateNoteCommand command) {
        Note note = findEditableNote(command.id());
        requireOwner(note, command.authorUserId());

        return repository.updateOwned(command)
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));
    }

    public void deleteNote(Long id, String authorUserId) {
        Note note = findEditableNote(id);
        requireOwner(note, authorUserId);

        if (!repository.softDeleteOwned(id, authorUserId)) {
            throw new CoreException(NOTE_NOT_FOUND);
        }
    }

    public List<Note> findNearbyNotes(NearbyNotesCondition condition, String viewerUserId) {
        return repository.findNearbyAccessible(condition, viewerUserId);
    }

    private Note findEditableNote(Long id) {
        Note note = repository.findById(id)
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));

        if (note.status() != NoteStatus.ACTIVE) {
            throw new CoreException(NOTE_NOT_ACTIVE);
        }

        return note;
    }

    private static void requireOwner(Note note, String authorUserId) {
        if (!note.authorUserId().equals(authorUserId)) {
            throw new CoreException(NOTE_ACCESS_DENIED);
        }
    }
}
