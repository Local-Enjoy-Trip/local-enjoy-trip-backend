package com.ssafy.enjoytrip.storage.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jooq.Condition;
import org.jooq.SQLDialect;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseStorageRepositoryTest {

    @DisplayName("브리핑 코스 후보 조건은 공개 READY 미삭제 코스만 허용한다")
    @Test
    void publicReadyCandidateConditionAllowsOnlyPublicReadyNotDeletedCourses() {
        String sql = normalizedSql(CourseStorageRepository.publicReadyCandidateCondition());

        assertTrue(sql.contains("courses.visibility = PUBLIC"));
        assertTrue(sql.contains("courses.status = READY"));
        assertTrue(sql.contains("courses.deleted_at is null"));
        assertFalse(sql.contains("PRIVATE"));
        assertFalse(sql.contains("DRAFT"));
        assertFalse(sql.contains("ARCHIVED"));
    }

    @DisplayName("지역명이 있으면 같은 지역 코스를 먼저 정렬한다")
    @Test
    void regionMatchFirstSortsSameRegionBeforeFallbackRows() {
        String sql = normalizedSql(CourseStorageRepository.regionMatchFirst("서울"));

        assertTrue(sql.contains("case when courses.region_name = 서울 then 0 else 1 end asc"));
    }

    @DisplayName("지역명이 없으면 지역 우선순위를 적용하지 않는다")
    @Test
    void blankRegionDoesNotApplyRegionPriority() {
        String sql = normalizedSql(CourseStorageRepository.regionMatchFirst(""));

        assertTrue(sql.contains("0 asc"));
        assertFalse(sql.contains("courses.region_name"));
    }

    private static String normalizedSql(Condition condition) {
        return DSL.using(SQLDialect.POSTGRES)
                .renderInlined(condition)
                .replace("\"", "")
                .replace("'", "");
    }

    private static String normalizedSql(SortField<?> sortField) {
        return DSL.using(SQLDialect.POSTGRES)
                .renderInlined(sortField)
                .replace("\"", "")
                .replace("'", "");
    }
}
