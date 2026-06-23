package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.NOTE_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteStatus;
import com.ssafy.enjoytrip.core.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.query.MapNotesCondition;
import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteMapper noteMapper;

    public Note createNote(Note note) {
        NoteRecord record = new NoteRecord(
                note.authorMemberId(),
                note.title(),
                note.content(),
                note.category().name(),
                note.visibility().name(),
                BigDecimal.valueOf(note.latitude()),
                BigDecimal.valueOf(note.longitude()),
                note.regionName(),
                note.imageObjectKey(),
                note.imageUrl(),
                note.imageContentType()
        );
        NoteRecord saved = noteMapper.insert(record);

        return toNote(saved);
    }

    @Transactional
    public Note updateNote(Note requestedNote) {
        Note note = findNoteById(requestedNote.id())
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));
        note.requireEditableBy(requestedNote.authorMemberId());

        NoteRecord record = new NoteRecord(
                requestedNote.id(),
                requestedNote.authorMemberId(),
                requestedNote.title(),
                requestedNote.content(),
                requestedNote.category().name(),
                requestedNote.visibility().name(),
                BigDecimal.valueOf(requestedNote.latitude()),
                BigDecimal.valueOf(requestedNote.longitude()),
                requestedNote.regionName(),
                requestedNote.imageObjectKey(),
                requestedNote.imageUrl(),
                requestedNote.imageContentType()
        );
        NoteRecord updated = noteMapper.updateOwned(record);
        if (updated == null) {
            throw new CoreException(NOTE_NOT_FOUND);
        }

        return toNote(updated);
    }

    @Transactional
    public void deleteNote(Long id, Long authorMemberId) {
        Note note = findNoteById(id)
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));
        note.requireEditableBy(authorMemberId);

        if (noteMapper.softDeleteOwned(id, authorMemberId) <= 0) {
            throw new CoreException(NOTE_NOT_FOUND);
        }
    }

    @Transactional
    public void addSave(Long noteId, Long memberId) {
        requireAccessibleActiveNote(noteId, memberId);
        noteMapper.insertSave(noteId, memberId);
    }

    public boolean removeSave(Long noteId, Long memberId) {
        return noteMapper.deleteSave(noteId, memberId) > 0;
    }

    public List<Note> findSavedNotes(Long memberId, int limit) {
        return noteMapper.findSavedAccessible(memberId, limit).stream()
                .map(this::toNote)
                .toList();
    }

    public List<Note> findNearbyNotes(DistanceSearchCondition condition, Long viewerMemberId) {
        return noteMapper.findNearbyAccessible(
                        condition.longitude(),
                        condition.latitude(),
                        condition.radiusMeters(),
                        condition.limit(),
                        viewerMemberId
                ).stream()
                .map(this::toNote)
                .toList();
    }

    public List<NoteMapPin> findMapNotes(MapNotesCondition condition) {
        return noteMapper.findMapPins(
                        condition.longitude(),
                        condition.latitude(),
                        condition.radiusMeters(),
                        condition.limit(),
                        condition.viewerMemberId(),
                        condition.category() == null ? null : condition.category().name(),
                        condition.friendOnly()
                ).stream()
                .map(this::toNoteMapPin)
                .toList();
    }

    private void requireAccessibleActiveNote(Long noteId, Long memberId) {
        if (noteMapper.existsAccessibleActive(noteId, memberId) <= 0) {
            throw new CoreException(NOTE_NOT_FOUND);
        }
    }

    private Optional<Note> findNoteById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        NoteRecord record = noteMapper.findById(id);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(toNote(record));
    }

    private Note toNote(NoteRecord record) {
        return new Note(
                record.getId(),
                record.getAuthorMemberId(),
                record.getTitle(),
                record.getContent(),
                NoteCategory.valueOf(record.getCategory()),
                NoteVisibility.valueOf(record.getVisibility()),
                record.getLatitude().doubleValue(),
                record.getLongitude().doubleValue(),
                record.getRegionName(),
                record.getImageObjectKey(),
                record.getImageUrl(),
                record.getImageContentType(),
                NoteStatus.valueOf(record.getStatus()),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getDeletedAt()
        );
    }

    private NoteMapPin toNoteMapPin(NoteMapPinRecord record) {
        return new NoteMapPin(
                record.id(),
                record.title(),
                NoteCategory.valueOf(record.category()),
                NoteVisibility.valueOf(record.visibility()),
                record.latitude().doubleValue(),
                record.longitude().doubleValue(),
                record.regionName(),
                record.distanceMeters(),
                record.imageObjectKey(),
                record.authorNickname(),
                record.authorProfileImageUrl(),
                NoteViewerRelationship.valueOf(record.relationship()),
                record.createdAt()
        );
    }

}
