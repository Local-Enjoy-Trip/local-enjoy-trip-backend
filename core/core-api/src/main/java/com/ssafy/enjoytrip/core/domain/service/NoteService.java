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
import com.ssafy.enjoytrip.storage.db.core.entity.NoteEntity;
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
        NoteEntity entity = new NoteEntity();
        entity.setAuthorUserId(note.authorUserId());
        entity.setTitle(note.title());
        entity.setContent(note.content());
        entity.setCategory(note.category().name());
        entity.setVisibility(note.visibility().name());
        entity.setLatitude(BigDecimal.valueOf(note.latitude()));
        entity.setLongitude(BigDecimal.valueOf(note.longitude()));
        entity.setRegionName(blankToNull(note.regionName()));
        entity.setImageObjectKey(blankToNull(note.imageObjectKey()));
        entity.setImageUrl(blankToNull(note.imageUrl()));
        entity.setImageContentType(blankToNull(note.imageContentType()));
        NoteEntity saved = noteMapper.insert(entity);

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

        NoteEntity entity = new NoteEntity();
        entity.setId(requestedNote.id());
        entity.setAuthorUserId(requestedNote.authorUserId());
        entity.setTitle(requestedNote.title());
        entity.setContent(requestedNote.content());
        entity.setCategory(requestedNote.category().name());
        entity.setVisibility(requestedNote.visibility().name());
        entity.setLatitude(BigDecimal.valueOf(requestedNote.latitude()));
        entity.setLongitude(BigDecimal.valueOf(requestedNote.longitude()));
        entity.setRegionName(blankToNull(requestedNote.regionName()));
        entity.setImageObjectKey(blankToNull(requestedNote.imageObjectKey()));
        entity.setImageUrl(blankToNull(requestedNote.imageUrl()));
        entity.setImageContentType(blankToNull(requestedNote.imageContentType()));
        NoteEntity updated = noteMapper.updateOwned(entity);
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
                .map(entity -> new Note(
                        entity.getId(),
                        entity.getAuthorUserId(),
                        entity.getTitle(),
                        entity.getContent(),
                        NoteCategory.valueOf(entity.getCategory()),
                        NoteVisibility.valueOf(entity.getVisibility()),
                        entity.getLatitude().doubleValue(),
                        entity.getLongitude().doubleValue(),
                        entity.getRegionName(),
                        entity.getImageObjectKey(),
                        entity.getImageUrl(),
                        entity.getImageContentType(),
                        NoteStatus.valueOf(entity.getStatus()),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt(),
                        entity.getDeletedAt()
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
                .map(row -> new NoteMapPin(
                        row.id(),
                        row.title(),
                        NoteCategory.valueOf(row.category()),
                        NoteVisibility.valueOf(row.visibility()),
                        row.latitude().doubleValue(),
                        row.longitude().doubleValue(),
                        row.regionName(),
                        row.distanceMeters(),
                        row.imageObjectKey(),
                        row.authorUserId(),
                        row.authorNickname(),
                        row.authorProfileImageUrl(),
                        NoteViewerRelationship.valueOf(row.relationship()),
                        row.createdAt()
                ))
                .toList();
    }

    private Optional<Note> findNoteById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        NoteEntity entity = noteMapper.findById(id);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new Note(
                entity.getId(),
                entity.getAuthorUserId(),
                entity.getTitle(),
                entity.getContent(),
                NoteCategory.valueOf(entity.getCategory()),
                NoteVisibility.valueOf(entity.getVisibility()),
                entity.getLatitude().doubleValue(),
                entity.getLongitude().doubleValue(),
                entity.getRegionName(),
                entity.getImageObjectKey(),
                entity.getImageUrl(),
                entity.getImageContentType(),
                NoteStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        ));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
