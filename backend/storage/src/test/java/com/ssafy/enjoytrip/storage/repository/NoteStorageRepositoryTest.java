package com.ssafy.enjoytrip.storage.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jooq.Condition;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteStorageRepositoryTest {

    @DisplayName("익명 주변 쪽지 조회는 공개 쪽지만 허용하는 조건을 만든다")
    @Test
    void anonymousVisibilityConditionAllowsPublicOnly() {
        String sql = normalizedSql(NoteStorageRepository.visibilityCondition(null));

        assertTrue(sql.contains("notes.visibility = PUBLIC"));
        assertFalse(sql.contains("friendships"));
        assertFalse(sql.contains("notes.author_user_id"));
    }

    @DisplayName("인증 주변 쪽지 조회는 공개 본인 친구공개 accepted 친구를 허용한다")
    @Test
    void authenticatedVisibilityConditionAllowsOwnedAndAcceptedFriends() {
        String sql = normalizedSql(NoteStorageRepository.visibilityCondition("viewer"));

        assertTrue(sql.contains("notes.visibility = PUBLIC"));
        assertTrue(sql.contains("notes.author_user_id = viewer"));
        assertTrue(sql.contains("notes.visibility = FRIENDS"));
        assertTrue(sql.contains("friendships.status = ACCEPTED"));
        assertTrue(sql.contains("friendships.requester_user_id = viewer"));
        assertTrue(sql.contains("friendships.addressee_user_id = viewer"));
    }

    private static String normalizedSql(Condition condition) {
        return DSL.using(SQLDialect.POSTGRES)
                .renderInlined(condition)
                .replace("\"", "")
                .replace("'", "");
    }
}
