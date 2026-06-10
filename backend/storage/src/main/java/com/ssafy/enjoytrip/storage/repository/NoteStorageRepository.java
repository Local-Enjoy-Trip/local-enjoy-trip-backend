package com.ssafy.enjoytrip.storage.repository;

import static com.ssafy.enjoytrip.storage.jooq.tables.Friendships.FRIENDSHIPS;
import static com.ssafy.enjoytrip.storage.jooq.tables.Notes.NOTES;
import static org.jooq.impl.DSL.currentLocalDateTime;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.selectOne;

import com.ssafy.enjoytrip.domain.CreateNoteCommand;
import com.ssafy.enjoytrip.domain.NearbyNotesCondition;
import com.ssafy.enjoytrip.domain.Note;
import com.ssafy.enjoytrip.domain.NoteCategory;
import com.ssafy.enjoytrip.domain.NoteStatus;
import com.ssafy.enjoytrip.domain.NoteVisibility;
import com.ssafy.enjoytrip.domain.UpdateNoteCommand;
import com.ssafy.enjoytrip.repository.NoteRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class NoteStorageRepository implements NoteRepository {
    private static final String ACTIVE = NoteStatus.ACTIVE.name();
    private static final String DELETED = NoteStatus.DELETED.name();
    private static final String ACCEPTED = "ACCEPTED";

    private final DSLContext dslContext;

    @Override
    @Transactional
    public Note save(CreateNoteCommand command) {
        return dslContext.insertInto(NOTES)
                .set(NOTES.AUTHOR_USER_ID, command.authorUserId())
                .set(NOTES.TITLE, command.title())
                .set(NOTES.CONTENT, command.content())
                .set(NOTES.CATEGORY, command.category().name())
                .set(NOTES.VISIBILITY, command.visibility().name())
                .set(NOTES.LOCATION, point(command.longitude(), command.latitude()))
                .set(NOTES.LATITUDE, BigDecimal.valueOf(command.latitude()))
                .set(NOTES.LONGITUDE, BigDecimal.valueOf(command.longitude()))
                .set(NOTES.REGION_NAME, blankToNull(command.regionName()))
                .returning()
                .fetchOne(NoteStorageRepository::toNote);
    }

    @Override
    public Optional<Note> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return dslContext.selectFrom(NOTES)
                .where(NOTES.ID.eq(id))
                .fetchOptional(NoteStorageRepository::toNote);
    }

    @Override
    @Transactional
    public Optional<Note> updateOwned(UpdateNoteCommand command) {
        return dslContext.update(NOTES)
                .set(NOTES.TITLE, command.title())
                .set(NOTES.CONTENT, command.content())
                .set(NOTES.CATEGORY, command.category().name())
                .set(NOTES.VISIBILITY, command.visibility().name())
                .set(NOTES.LOCATION, point(command.longitude(), command.latitude()))
                .set(NOTES.LATITUDE, BigDecimal.valueOf(command.latitude()))
                .set(NOTES.LONGITUDE, BigDecimal.valueOf(command.longitude()))
                .set(NOTES.REGION_NAME, blankToNull(command.regionName()))
                .set(NOTES.UPDATED_AT, currentLocalDateTime())
                .where(NOTES.ID.eq(command.id()))
                .and(NOTES.AUTHOR_USER_ID.eq(command.authorUserId()))
                .and(NOTES.STATUS.eq(ACTIVE))
                .returning()
                .fetchOptional(NoteStorageRepository::toNote);
    }

    @Override
    @Transactional
    public boolean softDeleteOwned(Long id, String authorUserId) {
        return dslContext.update(NOTES)
                .set(NOTES.STATUS, DELETED)
                .set(NOTES.UPDATED_AT, currentLocalDateTime())
                .set(NOTES.DELETED_AT, currentLocalDateTime())
                .where(NOTES.ID.eq(id))
                .and(NOTES.AUTHOR_USER_ID.eq(authorUserId))
                .and(NOTES.STATUS.eq(ACTIVE))
                .execute() > 0;
    }

    @Override
    public List<Note> findNearbyAccessible(NearbyNotesCondition condition, String viewerUserId) {
        String viewer = blankToNull(viewerUserId);
        var point = point(condition.longitude(), condition.latitude());
        return dslContext.selectFrom(NOTES)
                .where(NOTES.STATUS.eq(ACTIVE))
                .and("ST_DWithin({0}::geography, {1}::geography, {2})",
                        NOTES.LOCATION, point, condition.radiusMeters())
                .and(visibilityCondition(viewer))
                .orderBy(NOTES.CREATED_AT.desc(), NOTES.ID.desc())
                .limit(condition.limit())
                .fetch(NoteStorageRepository::toNote);
    }

    static Condition visibilityCondition(String viewerUserId) {
        if (viewerUserId == null) {
            return NOTES.VISIBILITY.eq(NoteVisibility.PUBLIC.name());
        }
        return NOTES.VISIBILITY.eq(NoteVisibility.PUBLIC.name())
                .or(NOTES.AUTHOR_USER_ID.eq(viewerUserId))
                .or(NOTES.VISIBILITY.eq(NoteVisibility.FRIENDS.name()).and(acceptedFriendshipExists(viewerUserId)));
    }

    private static Condition acceptedFriendshipExists(String viewerUserId) {
        return exists(selectOne()
                .from(FRIENDSHIPS)
                .where(FRIENDSHIPS.STATUS.eq(ACCEPTED))
                .and(
                        FRIENDSHIPS.REQUESTER_USER_ID.eq(viewerUserId).and(FRIENDSHIPS.ADDRESSEE_USER_ID.eq(NOTES.AUTHOR_USER_ID))
                                .or(FRIENDSHIPS.REQUESTER_USER_ID.eq(NOTES.AUTHOR_USER_ID).and(FRIENDSHIPS.ADDRESSEE_USER_ID.eq(viewerUserId)))
                ));
    }

    private static Field<String> point(double longitude, double latitude) {
        return field("ST_SetSRID(ST_MakePoint({0}, {1}), 4326)", String.class, longitude, latitude);
    }

    private static Note toNote(Record record) {
        return new Note(
                record.get(NOTES.ID),
                record.get(NOTES.AUTHOR_USER_ID),
                record.get(NOTES.TITLE),
                record.get(NOTES.CONTENT),
                NoteCategory.valueOf(record.get(NOTES.CATEGORY)),
                NoteVisibility.valueOf(record.get(NOTES.VISIBILITY)),
                record.get(NOTES.LATITUDE).doubleValue(),
                record.get(NOTES.LONGITUDE).doubleValue(),
                record.get(NOTES.REGION_NAME),
                NoteStatus.valueOf(record.get(NOTES.STATUS)),
                record.get(NOTES.CREATED_AT),
                record.get(NOTES.UPDATED_AT),
                record.get(NOTES.DELETED_AT)
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
