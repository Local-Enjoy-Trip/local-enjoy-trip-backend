package com.ssafy.enjoytrip.storage.repository;

import static com.ssafy.enjoytrip.storage.jooq.tables.Friendships.FRIENDSHIPS;
import static com.ssafy.enjoytrip.storage.jooq.tables.Members.MEMBERS;
import static com.ssafy.enjoytrip.storage.jooq.tables.Notes.NOTES;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.currentLocalDateTime;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.selectOne;
import static org.jooq.impl.DSL.when;

import com.ssafy.enjoytrip.dto.command.CreateNoteCommand;
import com.ssafy.enjoytrip.dto.query.MapNotesCondition;
import com.ssafy.enjoytrip.dto.query.NearbyNotesCondition;
import com.ssafy.enjoytrip.domain.Note;
import com.ssafy.enjoytrip.domain.NoteCategory;
import com.ssafy.enjoytrip.domain.NoteImageReference;
import com.ssafy.enjoytrip.domain.NoteMapPin;
import com.ssafy.enjoytrip.domain.NoteStatus;
import com.ssafy.enjoytrip.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.domain.NoteVisibility;
import com.ssafy.enjoytrip.dto.command.UpdateNoteCommand;
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
                .set(NOTES.IMAGE_OBJECT_KEY, imageObjectKey(command.imageReference()))
                .set(NOTES.IMAGE_URL, imageUrl(command.imageReference()))
                .set(NOTES.IMAGE_CONTENT_TYPE, imageContentType(command.imageReference()))
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
                .set(NOTES.IMAGE_OBJECT_KEY, imageObjectKey(command.imageReference()))
                .set(NOTES.IMAGE_URL, imageUrl(command.imageReference()))
                .set(NOTES.IMAGE_CONTENT_TYPE, imageContentType(command.imageReference()))
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
        Field<String> centerPoint = point(condition.longitude(), condition.latitude());

        return dslContext.selectFrom(NOTES)
                .where(NOTES.STATUS.eq(ACTIVE))
                .and("ST_DWithin({0}::geography, {1}::geography, {2})",
                        NOTES.LOCATION, centerPoint, condition.radiusMeters())
                .and(visibilityCondition(viewer))
                .orderBy(NOTES.CREATED_AT.desc(), NOTES.ID.desc())
                .limit(condition.limit())
                .fetch(NoteStorageRepository::toNote);
    }

    @Override
    public List<NoteMapPin> findMapNotes(MapNotesCondition condition) {
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
        Condition queryCondition = NOTES.STATUS.eq(ACTIVE)
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
                .where(FRIENDSHIPS.STATUS.eq(ACCEPTED))
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

    private static String imageObjectKey(NoteImageReference reference) {
        return reference == null ? null : blankToNull(reference.objectKey());
    }

    private static String imageUrl(NoteImageReference reference) {
        return reference == null ? null : blankToNull(reference.publicUrl());
    }

    private static String imageContentType(NoteImageReference reference) {
        return reference == null ? null : blankToNull(reference.contentType());
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
