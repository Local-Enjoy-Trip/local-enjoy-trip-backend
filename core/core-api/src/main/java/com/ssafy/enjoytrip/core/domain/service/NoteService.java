package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.NOTE_NOT_FOUND;
import static com.ssafy.enjoytrip.storage.db.core.jooq.tables.Friendships.FRIENDSHIPS;
import static com.ssafy.enjoytrip.storage.db.core.jooq.tables.Members.MEMBERS;
import static com.ssafy.enjoytrip.storage.db.core.jooq.tables.Notes.NOTES;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.currentLocalDateTime;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.selectOne;
import static org.jooq.impl.DSL.when;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteImageReference;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.NoteStatus;
import com.ssafy.enjoytrip.core.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.domain.query.MapNotesCondition;
import com.ssafy.enjoytrip.core.domain.query.NearbyNotesCondition;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteService {

    @Transactional
    public Note updateNote(Note requestedNote) {
        Note note = findNoteById(requestedNote.id())
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));
        note.requireEditableBy(requestedNote.authorUserId());

        return updateOwnedNote(requestedNote)
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));
    }

    @Transactional
    public void deleteNote(Long id, String authorUserId) {
        Note note = findNoteById(id)
                .orElseThrow(() -> new CoreException(NOTE_NOT_FOUND));
        note.requireEditableBy(authorUserId);

        if (!softDeleteOwnedNote(id, authorUserId)) {
            throw new CoreException(NOTE_NOT_FOUND);
        }
    }

    public List<Note> findNearbyNotes(NearbyNotesCondition condition, String viewerUserId) {
        return findNearbyAccessibleNotes(condition, viewerUserId);
    }

    public List<NoteMapPin> findMapNotes(MapNotesCondition condition) {
        return findMapNotePins(condition);
    }

    private final DSLContext dslContext;

    public Note createNote(Note note) {
        return dslContext.insertInto(NOTES)
                .set(NOTES.AUTHOR_USER_ID, note.authorUserId())
                .set(NOTES.TITLE, note.title())
                .set(NOTES.CONTENT, note.content())
                .set(NOTES.CATEGORY, note.category().name())
                .set(NOTES.VISIBILITY, note.visibility().name())
                .set(NOTES.LOCATION, point(note.longitude(), note.latitude()))
                .set(NOTES.LATITUDE, BigDecimal.valueOf(note.latitude()))
                .set(NOTES.LONGITUDE, BigDecimal.valueOf(note.longitude()))
                .set(NOTES.REGION_NAME, blankToNull(note.regionName()))
                .set(NOTES.IMAGE_OBJECT_KEY, blankToNull(note.imageObjectKey()))
                .set(NOTES.IMAGE_URL, blankToNull(note.imageUrl()))
                .set(NOTES.IMAGE_CONTENT_TYPE, blankToNull(note.imageContentType()))
                .returning()
                .fetchOne(NoteService::toNote);
    }

    private Optional<Note> findNoteById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return dslContext.selectFrom(NOTES)
                .where(NOTES.ID.eq(id))
                .fetchOptional(NoteService::toNote);
    }

    private Optional<Note> updateOwnedNote(Note note) {
        return dslContext.update(NOTES)
                .set(NOTES.TITLE, note.title())
                .set(NOTES.CONTENT, note.content())
                .set(NOTES.CATEGORY, note.category().name())
                .set(NOTES.VISIBILITY, note.visibility().name())
                .set(NOTES.LOCATION, point(note.longitude(), note.latitude()))
                .set(NOTES.LATITUDE, BigDecimal.valueOf(note.latitude()))
                .set(NOTES.LONGITUDE, BigDecimal.valueOf(note.longitude()))
                .set(NOTES.REGION_NAME, blankToNull(note.regionName()))
                .set(NOTES.IMAGE_OBJECT_KEY, blankToNull(note.imageObjectKey()))
                .set(NOTES.IMAGE_URL, blankToNull(note.imageUrl()))
                .set(NOTES.IMAGE_CONTENT_TYPE, blankToNull(note.imageContentType()))
                .set(NOTES.UPDATED_AT, currentLocalDateTime())
                .where(NOTES.ID.eq(note.id()))
                .and(NOTES.AUTHOR_USER_ID.eq(note.authorUserId()))
                .and(NOTES.STATUS.eq(NoteStatus.ACTIVE.name()))
                .returning()
                .fetchOptional(NoteService::toNote);
    }

    private boolean softDeleteOwnedNote(Long id, String authorUserId) {
        return dslContext.update(NOTES)
                .set(NOTES.STATUS, NoteStatus.DELETED.name())
                .set(NOTES.UPDATED_AT, currentLocalDateTime())
                .set(NOTES.DELETED_AT, currentLocalDateTime())
                .where(NOTES.ID.eq(id))
                .and(NOTES.AUTHOR_USER_ID.eq(authorUserId))
                .and(NOTES.STATUS.eq(NoteStatus.ACTIVE.name()))
                .execute() > 0;
    }

    private List<Note> findNearbyAccessibleNotes(NearbyNotesCondition condition, String viewerUserId) {
        String viewer = blankToNull(viewerUserId);
        Field<String> centerPoint = point(condition.longitude(), condition.latitude());

        return dslContext.selectFrom(NOTES)
                .where(NOTES.STATUS.eq(NoteStatus.ACTIVE.name()))
                .and("ST_DWithin({0}::geography, {1}::geography, {2})",
                        NOTES.LOCATION, centerPoint, condition.radiusMeters())
                .and(visibilityCondition(viewer))
                .orderBy(NOTES.CREATED_AT.desc(), NOTES.ID.desc())
                .limit(condition.limit())
                .fetch(NoteService::toNote);
    }

    private List<NoteMapPin> findMapNotePins(MapNotesCondition condition) {
        String viewer = blankToNull(condition.viewerUserId());
        Field<String> centerPoint = point(condition.longitude(), condition.latitude());
        Field<Double> distance = distanceMeters(centerPoint);
        Condition acceptedFriend = acceptedFriendshipExists(viewer);
        Field<String> relationship = relationshipField(viewer, acceptedFriend);
        Field<String> authorProfileImageUrl = authorProfileImageUrlField(viewer, acceptedFriend);

        return dslContext.select(
                        NOTES.ID,
                        NOTES.TITLE,
                        NOTES.CATEGORY,
                        NOTES.VISIBILITY,
                        NOTES.LATITUDE,
                        NOTES.LONGITUDE,
                        NOTES.REGION_NAME,
                        NOTES.IMAGE_OBJECT_KEY,
                        NOTES.AUTHOR_USER_ID,
                        coalesce(MEMBERS.NICKNAME, MEMBERS.NAME, MEMBERS.USER_ID).as("author_nickname"),
                        authorProfileImageUrl,
                        relationship,
                        NOTES.CREATED_AT,
                        distance
                )
                .from(NOTES)
                .join(MEMBERS).on(MEMBERS.USER_ID.eq(NOTES.AUTHOR_USER_ID))
                .where(mapNotesCondition(condition, centerPoint, viewer, acceptedFriend))
                .orderBy(distance.asc(), NOTES.CREATED_AT.desc(), NOTES.ID.desc())
                .limit(condition.limit())
                .fetch(record -> toNoteMapPin(record, distance, relationship, authorProfileImageUrl));
    }

    static Condition visibilityCondition(String viewerUserId) {
        if (viewerUserId == null) {
            return NOTES.VISIBILITY.eq(NoteVisibility.PUBLIC.name());
        }

        return NOTES.VISIBILITY.eq(NoteVisibility.PUBLIC.name())
                .or(NOTES.AUTHOR_USER_ID.eq(viewerUserId))
                .or(NOTES.VISIBILITY.eq(NoteVisibility.FRIENDS.name())
                        .and(acceptedFriendshipExists(viewerUserId)));
    }

    static Condition mapNotesCondition(MapNotesCondition condition,
                                               Field<String> centerPoint,
                                               String viewerUserId,
                                               Condition acceptedFriend) {
        Condition queryCondition = NOTES.STATUS.eq(NoteStatus.ACTIVE.name())
                .and("ST_DWithin({0}::geography, {1}::geography, {2})",
                        NOTES.LOCATION, centerPoint, condition.radiusMeters())
                .and(visibilityCondition(viewerUserId));

        if (condition.category() != null) {
            queryCondition = queryCondition.and(NOTES.CATEGORY.eq(condition.category().name()));
        }
        if (condition.friendOnly()) {
            queryCondition = queryCondition
                    .and(NOTES.AUTHOR_USER_ID.ne(viewerUserId))
                    .and(acceptedFriend);
        }

        return queryCondition;
    }

    private static Condition acceptedFriendshipExists(String viewerUserId) {
        if (viewerUserId == null) {
            return falseCondition();
        }

        return exists(selectOne()
                .from(FRIENDSHIPS)
                .where(FRIENDSHIPS.STATUS.eq(FriendshipStatus.ACCEPTED.name()))
                .and(
                        FRIENDSHIPS.REQUESTER_USER_ID.eq(viewerUserId)
                                .and(FRIENDSHIPS.ADDRESSEE_USER_ID.eq(NOTES.AUTHOR_USER_ID))
                                .or(FRIENDSHIPS.REQUESTER_USER_ID.eq(NOTES.AUTHOR_USER_ID)
                                        .and(FRIENDSHIPS.ADDRESSEE_USER_ID.eq(viewerUserId)))
                ));
    }

    private static Field<String> relationshipField(String viewerUserId, Condition acceptedFriend) {
        if (viewerUserId == null) {
            return inline(NoteViewerRelationship.NONE.name()).as("relationship_to_viewer");
        }

        return when(NOTES.AUTHOR_USER_ID.eq(viewerUserId), inline(NoteViewerRelationship.SELF.name()))
                .when(acceptedFriend, inline(NoteViewerRelationship.FRIEND.name()))
                .otherwise(inline(NoteViewerRelationship.NONE.name()))
                .as("relationship_to_viewer");
    }

    private static Field<String> authorProfileImageUrlField(String viewerUserId, Condition acceptedFriend) {
        if (viewerUserId == null) {
            return inline((String) null).as("author_profile_image_url");
        }

        return when(NOTES.AUTHOR_USER_ID.eq(viewerUserId).or(acceptedFriend), MEMBERS.PROFILE_IMAGE_URL)
                .otherwise(inline((String) null))
                .as("author_profile_image_url");
    }

    private static NoteMapPin toNoteMapPin(Record record,
                                           Field<Double> distance,
                                           Field<String> relationship,
                                           Field<String> authorProfileImageUrl) {
        return new NoteMapPin(
                record.get(NOTES.ID),
                record.get(NOTES.TITLE),
                NoteCategory.valueOf(record.get(NOTES.CATEGORY)),
                NoteVisibility.valueOf(record.get(NOTES.VISIBILITY)),
                record.get(NOTES.LATITUDE).doubleValue(),
                record.get(NOTES.LONGITUDE).doubleValue(),
                record.get(NOTES.REGION_NAME),
                record.get(distance),
                record.get(NOTES.IMAGE_OBJECT_KEY),
                record.get(NOTES.AUTHOR_USER_ID),
                record.get("author_nickname", String.class),
                record.get(authorProfileImageUrl),
                NoteViewerRelationship.valueOf(record.get(relationship)),
                record.get(NOTES.CREATED_AT)
        );
    }

    private static Field<Double> distanceMeters(Field<String> centerPoint) {
        return field(
                "ST_Distance({0}::geography, {1}::geography)",
                Double.class,
                NOTES.LOCATION,
                centerPoint
        ).as("distance_meters");
    }

    static Field<String> point(double longitude, double latitude) {
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
                record.get(NOTES.IMAGE_OBJECT_KEY),
                record.get(NOTES.IMAGE_URL),
                record.get(NOTES.IMAGE_CONTENT_TYPE),
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
