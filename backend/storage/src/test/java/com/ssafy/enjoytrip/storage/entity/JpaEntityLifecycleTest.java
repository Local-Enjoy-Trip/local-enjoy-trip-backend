package com.ssafy.enjoytrip.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaEntityLifecycleTest {
    @Test
    void authLogPrePersistInitializesLoggedAtAndKeepsTableMapping() {
        AuthLogEntity authLog = new AuthLogEntity("ssafy", "LOGIN");

        authLog.prePersist();

        assertAll(
                () -> assertEquals("auth_logs", AuthLogEntity.class.getAnnotation(Table.class).name()),
                () -> assertEquals("ssafy", field(authLog, "userId")),
                () -> assertEquals("LOGIN", field(authLog, "eventType")),
                () -> assertNotNull(field(authLog, "loggedAt")),
                () -> assertColumn(AuthLogEntity.class, "userId", "user_id", 64, false)
        );
    }

    @Test
    void boardPrePersistAndPreUpdateManageTimestampsWithoutOverwritingCreatedAt() {
        BoardPostEntity board = new BoardPostEntity("board-1", "Title", "content", "author");
        LocalDateTime existingCreatedAt = LocalDateTime.parse("2026-05-15T09:00:00");
        setField(board, "createdAt", existingCreatedAt);

        board.prePersist();
        board.update("Updated", "updated content");
        board.preUpdate();

        assertAll(
                () -> assertEquals("boards", BoardPostEntity.class.getAnnotation(Table.class).name()),
                () -> assertEquals(existingCreatedAt, board.getCreatedAt()),
                () -> assertEquals("Updated", board.getTitle()),
                () -> assertEquals("updated content", board.getContent()),
                () -> assertEquals("author", board.getAuthor()),
                () -> assertNotNull(board.getUpdatedAt()),
                () -> assertColumn(BoardPostEntity.class, "content", "", 255, false)
        );
    }

    @Test
    void memberUpdateIgnoresNullAndBlankPatchFieldsButRefreshesUpdatedAt() {
        MemberEntity member = new MemberEntity("ssafy", "SSAFY", "ssafy@example.com", "secret");

        member.prePersist();
        member.update("  ", null, "");

        assertAll(
                () -> assertEquals("members", MemberEntity.class.getAnnotation(Table.class).name()),
                () -> assertEquals("ssafy", member.getUserId()),
                () -> assertEquals("SSAFY", member.getName()),
                () -> assertEquals("ssafy@example.com", member.getEmail()),
                () -> assertEquals("secret", member.getPassword()),
                () -> assertNotNull(member.getCreatedAt()),
                () -> assertNotNull(field(member, "updatedAt")),
                () -> assertTrue(column(MemberEntity.class, "userId").unique()),
                () -> assertColumn(MemberEntity.class, "userId", "user_id", 64, false)
        );
    }

    @Test
    void noticePrePersistAndPreUpdateManageCreatedAndUpdatedTimestamps() {
        NoticeEntity notice = new NoticeEntity("Notice", "content", "admin");

        notice.prePersist();
        notice.update("Updated", "updated content");
        notice.preUpdate();

        assertAll(
                () -> assertEquals("notices", NoticeEntity.class.getAnnotation(Table.class).name()),
                () -> assertEquals("Updated", notice.getTitle()),
                () -> assertEquals("updated content", notice.getContent()),
                () -> assertEquals("admin", notice.getAuthor()),
                () -> assertNotNull(notice.getCreatedAt()),
                () -> assertNotNull(notice.getUpdatedAt())
        );
    }

    @Test
    void hotplaceAndTravelPlanPreserveNullableTextFieldsAndRequiredColumns() {
        HotplaceEntity hotplace = new HotplaceEntity("hot-1", "ssafy", "Cafe", "food",
                "2026-05-15", 37.5, 127.0, null, null);
        TravelPlanEntity plan = new TravelPlanEntity("plan-1", "ssafy", "Trip", "2026-05-15",
                "2026-05-16", 1000, null, null);

        hotplace.prePersist();
        plan.prePersist();

        assertAll(
                () -> assertEquals("hotplaces", HotplaceEntity.class.getAnnotation(Table.class).name()),
                () -> assertEquals("plans", TravelPlanEntity.class.getAnnotation(Table.class).name()),
                () -> assertNull(hotplace.getDescription()),
                () -> assertNull(hotplace.getPhoto()),
                () -> assertNull(plan.getNote()),
                () -> assertNull(plan.getRouteItemsJson()),
                () -> assertNotNull(hotplace.getCreatedAt()),
                () -> assertNotNull(plan.getCreatedAt()),
                () -> assertColumn(HotplaceEntity.class, "userId", "user_id", 64, false),
                () -> assertColumn(TravelPlanEntity.class, "routeItemsJson", "route_items", 255, true)
        );
    }

    @Test
    void generatedIdsUseIdentityStrategyForDatabaseAssignedEntities() throws Exception {
        assertAll(
                () -> assertEquals(GenerationType.IDENTITY,
                        AuthLogEntity.class.getDeclaredField("id").getAnnotation(GeneratedValue.class).strategy()),
                () -> assertEquals(GenerationType.IDENTITY,
                        MemberEntity.class.getDeclaredField("id").getAnnotation(GeneratedValue.class).strategy()),
                () -> assertEquals(GenerationType.IDENTITY,
                        NoticeEntity.class.getDeclaredField("id").getAnnotation(GeneratedValue.class).strategy())
        );
    }

    private static void assertColumn(Class<?> type, String fieldName, String expectedName, int expectedLength, boolean nullable) {
        Column column = column(type, fieldName);
        assertEquals(expectedName, column.name());
        assertEquals(expectedLength, column.length());
        assertEquals(nullable, column.nullable());
    }

    private static Column column(Class<?> type, String fieldName) {
        try {
            return type.getDeclaredField(fieldName).getAnnotation(Column.class);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(ex);
        }
    }

    private static Object field(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
