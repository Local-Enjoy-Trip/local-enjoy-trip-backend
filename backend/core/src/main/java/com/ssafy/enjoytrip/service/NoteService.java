package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.NOTE_NOT_FOUND;

import com.ssafy.enjoytrip.application.dto.command.CreateNoteCommand;
import com.ssafy.enjoytrip.application.dto.command.UpdateNoteCommand;
import com.ssafy.enjoytrip.application.dto.query.NearbyNotesCondition;
import com.ssafy.enjoytrip.domain.Note;
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
        Note note = findRequiredNote(command.id());
        note.requireEditableBy(command.authorUserId());

        return repository.updateOwned(command)
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));
    }

    public void deleteNote(Long id, String authorUserId) {
        Note note = findRequiredNote(id);
        note.requireEditableBy(authorUserId);

        if (!repository.softDeleteOwned(id, authorUserId)) {
            throw new CoreException(NOTE_NOT_FOUND);
        }
    }

    public List<Note> findNearbyNotes(NearbyNotesCondition condition, String viewerUserId) {
        return repository.findNearbyAccessible(condition, viewerUserId);
    }

    private Note findRequiredNote(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));
    }
}
