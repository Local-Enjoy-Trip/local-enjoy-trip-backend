package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.NOTE_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.NoteStatus;
import com.ssafy.enjoytrip.core.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import com.ssafy.enjoytrip.core.domain.query.MapNotesCondition;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import com.ssafy.enjoytrip.external.minio.MinioNoteImageUploadUrlGenerator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {
    private static final String INVALID_NOTE_IMAGE_OBJECT_KEY_MESSAGE = "쪽지 이미지 경로가 올바르지 않습니다.";
    private static final Pattern NOTE_IMAGE_FILE_NAME_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}"
                    + "-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.[A-Za-z0-9]{1,10}$"
    );

    private final NoteMapper noteMapper;
    private final MinioNoteImageUploadUrlGenerator noteImageUploadUrlGenerator;

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
                noteImageObjectKey(note.authorMemberId(), note.imageObjectKey()),
                noteImagePublicUrl(note.authorMemberId(), note.imageObjectKey()),
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
                noteImageObjectKey(requestedNote.authorMemberId(), requestedNote.imageObjectKey()),
                noteImagePublicUrl(requestedNote.authorMemberId(), requestedNote.imageObjectKey()),
                requestedNote.imageContentType()
        );
        NoteRecord updated = noteMapper.updateOwned(record);
        if (updated == null) {
            throw new CoreException(NOTE_NOT_FOUND);
        }

        return toNote(updated);
    }

    private String noteImageObjectKey(Long memberId, String objectKey) {
        if (objectKey == null) {
            return null;
        }

        String normalized = objectKey.strip();
        if (!matchesNoteImageObjectKey(memberId, normalized)) {
            throw new ClientInputException(INVALID_NOTE_IMAGE_OBJECT_KEY_MESSAGE);
        }

        return normalized;
    }

    private String noteImagePublicUrl(Long memberId, String objectKey) {
        String normalizedObjectKey = noteImageObjectKey(memberId, objectKey);
        if (normalizedObjectKey == null) {
            return null;
        }

        return noteImageUploadUrlGenerator.publicUrl(normalizedObjectKey);
    }

    private static boolean matchesNoteImageObjectKey(Long memberId, String objectKey) {
        String requiredPrefix = "notes/" + memberId + "/";
        if (!objectKey.startsWith(requiredPrefix)) {
            return false;
        }

        return NOTE_IMAGE_FILE_NAME_PATTERN.matcher(
                objectKey.substring(requiredPrefix.length())
        ).matches();
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

    public List<NoteMapPin> searchMapNotes(String keyword, String escapedKeyword, double longitude, double latitude, Double radiusMeters, NoteCategory category, Integer limit, Long viewerMemberId) {
        long t0 = System.nanoTime();
        String categoryStr = category == null ? null : category.name();
        List<NoteMapPinRecord> records = noteMapper.searchMapNotes(
                keyword,
                escapedKeyword,
                longitude,
                latitude,
                radiusMeters,
                categoryStr,
                limit,
                viewerMemberId
        );
        int rows = records.size();
        log.info("map-search notes keyword={} radius={} rows={} tookMs={}", keyword, radiusMeters, rows, (System.nanoTime() - t0) / 1_000_000.0);

        return records.stream()
                .map(r -> {
                    int tier = r.title().equalsIgnoreCase(keyword) ? 0 : 1;
                    return toNoteMapPin(r, tier);
                })
                .toList();
    }

    private NoteMapPin toNoteMapPin(NoteMapPinRecord record) {
        return toNoteMapPin(record, 0);
    }

    private NoteMapPin toNoteMapPin(NoteMapPinRecord record, int matchTier) {
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
                record.createdAt(),
                matchTier
        );
    }
}
