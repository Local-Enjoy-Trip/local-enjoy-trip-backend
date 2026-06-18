package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.NOTE_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.NoteStatus;
import com.ssafy.enjoytrip.core.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.domain.query.MapNotesCondition;
import com.ssafy.enjoytrip.core.domain.query.NearbyNotesCondition;
import com.ssafy.enjoytrip.core.support.error.CoreException;
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
        NoteRecord record = new NoteRecord();
        record.setAuthorUserId(note.authorUserId());
        record.setTitle(note.title());
        record.setContent(note.content());
        record.setCategory(note.category().name());
        record.setVisibility(note.visibility().name());
        record.setLatitude(BigDecimal.valueOf(note.latitude()));
        record.setLongitude(BigDecimal.valueOf(note.longitude()));
        record.setRegionName(blankToNull(note.regionName()));
        record.setImageObjectKey(blankToNull(note.imageObjectKey()));
        record.setImageUrl(blankToNull(note.imageUrl()));
        record.setImageContentType(blankToNull(note.imageContentType()));
        NoteRecord saved = noteMapper.insert(record);

        return new Note(
                saved.getId(),
                saved.getAuthorUserId(),
                saved.getTitle(),
                saved.getContent(),
                NoteCategory.valueOf(saved.getCategory()),
                NoteVisibility.valueOf(saved.getVisibility()),
                saved.getLatitude().doubleValue(),
                saved.getLongitude().doubleValue(),
                saved.getRegionName(),
                saved.getImageObjectKey(),
                saved.getImageUrl(),
                saved.getImageContentType(),
                NoteStatus.valueOf(saved.getStatus()),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.getDeletedAt()
        );
    }

    @Transactional
    public Note updateNote(Note requestedNote) {
        Note note = findNoteById(requestedNote.id())
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));
        note.requireEditableBy(requestedNote.authorUserId());

        NoteRecord record = new NoteRecord();
        record.setId(requestedNote.id());
        record.setAuthorUserId(requestedNote.authorUserId());
        record.setTitle(requestedNote.title());
        record.setContent(requestedNote.content());
        record.setCategory(requestedNote.category().name());
        record.setVisibility(requestedNote.visibility().name());
        record.setLatitude(BigDecimal.valueOf(requestedNote.latitude()));
        record.setLongitude(BigDecimal.valueOf(requestedNote.longitude()));
        record.setRegionName(blankToNull(requestedNote.regionName()));
        record.setImageObjectKey(blankToNull(requestedNote.imageObjectKey()));
        record.setImageUrl(blankToNull(requestedNote.imageUrl()));
        record.setImageContentType(blankToNull(requestedNote.imageContentType()));
        NoteRecord updated = noteMapper.updateOwned(record);
        if (updated == null) {
            throw new CoreException(NOTE_NOT_FOUND);
        }

        return new Note(
                updated.getId(),
                updated.getAuthorUserId(),
                updated.getTitle(),
                updated.getContent(),
                NoteCategory.valueOf(updated.getCategory()),
                NoteVisibility.valueOf(updated.getVisibility()),
                updated.getLatitude().doubleValue(),
                updated.getLongitude().doubleValue(),
                updated.getRegionName(),
                updated.getImageObjectKey(),
                updated.getImageUrl(),
                updated.getImageContentType(),
                NoteStatus.valueOf(updated.getStatus()),
                updated.getCreatedAt(),
                updated.getUpdatedAt(),
                updated.getDeletedAt()
        );
    }

    @Transactional
    public void deleteNote(Long id, String authorUserId) {
        Note note = findNoteById(id)
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));
        note.requireEditableBy(authorUserId);

        if (noteMapper.softDeleteOwned(id, authorUserId) <= 0) {
            throw new CoreException(NOTE_NOT_FOUND);
        }
    }

    public List<Note> findNearbyNotes(NearbyNotesCondition condition, String viewerUserId) {
        return noteMapper.findNearbyAccessible(
                        condition.longitude(),
                        condition.latitude(),
                        condition.radiusMeters(),
                        condition.limit(),
                        blankToNull(viewerUserId)
                ).stream()
                .map(record -> new Note(
                        record.getId(),
                        record.getAuthorUserId(),
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
                ))
                .toList();
    }

    public List<NoteMapPin> findMapNotes(MapNotesCondition condition) {
        return noteMapper.findMapPins(
                        condition.longitude(),
                        condition.latitude(),
                        condition.radiusMeters(),
                        condition.limit(),
                        blankToNull(condition.viewerUserId()),
                        condition.category() == null ? null : condition.category().name(),
                        condition.friendOnly()
                ).stream()
                .map(record -> new NoteMapPin(
                        record.id(),
                        record.title(),
                        NoteCategory.valueOf(record.category()),
                        NoteVisibility.valueOf(record.visibility()),
                        record.latitude().doubleValue(),
                        record.longitude().doubleValue(),
                        record.regionName(),
                        record.distanceMeters(),
                        record.imageObjectKey(),
                        record.authorUserId(),
                        record.authorNickname(),
                        record.authorProfileImageUrl(),
                        NoteViewerRelationship.valueOf(record.relationship()),
                        record.createdAt()
                ))
                .toList();
    }

    private Optional<Note> findNoteById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        NoteRecord record = noteMapper.findById(id);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(new Note(
                record.getId(),
                record.getAuthorUserId(),
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
        ));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
