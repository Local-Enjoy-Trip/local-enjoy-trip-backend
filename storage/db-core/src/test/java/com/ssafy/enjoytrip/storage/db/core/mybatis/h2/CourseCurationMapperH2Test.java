package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CourseCurationMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private MemberMapper memberMapper;

    @DisplayName("관리자 멤버가 소유한 코스는 createdByAdmin이 true로 조회된다")
    @Test
    void adminOwnedCourseHasCreatedByAdminTrue() {
        Long adminMemberId = seedMember("admin", "admin@example.com");
        jdbcTemplate.update("update members set role = 'ADMIN' where id = ?", adminMemberId);

        courseMapper.insert(new CourseRecord("course-admin", adminMemberId, "서울 산책", "서울", null));

        CourseRecord found = courseMapper.findById("course-admin");

        assertThat(memberMapper.findById(adminMemberId).getRole()).isEqualTo("ADMIN");
        assertThat(found.getId()).isEqualTo("course-admin");
        assertThat(found.getCreatedByAdmin()).isTrue();
    }

    @DisplayName("일반 멤버가 소유한 코스는 createdByAdmin이 false로 조회된다")
    @Test
    void userOwnedCourseHasCreatedByAdminFalse() {
        Long userMemberId = seedMember("user", "user@example.com");

        courseMapper.insert(new CourseRecord("course-user", userMemberId, "내 코스", "부산", null));

        CourseRecord found = courseMapper.findById("course-user");

        assertThat(found.getCreatedByAdmin()).isFalse();
    }

    @DisplayName("공개 코스 아이템 조회는 숨김 장소와 비공개 노트를 제외한다")
    @Test
    void publicCourseItemsExcludeHiddenPlaceAndPrivateNote() {
        Long adminMemberId = seedMember("admin", "admin@example.com");
        seedAttraction(1L, "공개 장소");
        seedAttraction(2L, "숨김 장소");
        jdbcTemplate.update("""
                update attractions
                set status = 'HIDDEN',
                    deleted_at = current_timestamp
                where id = 2
                """);
        jdbcTemplate.update("""
                insert into notes (
                    id, author_member_id, title, content, visibility, latitude, longitude, status
                )
                values (1, ?, '공개 노트', '내용', 'PUBLIC', 37.5, 127.0, 'ACTIVE')
                """, adminMemberId);
        jdbcTemplate.update("""
                insert into notes (
                    id, author_member_id, title, content, visibility, latitude, longitude, status
                )
                values (2, ?, '비공개 노트', '내용', 'PRIVATE', 37.5, 127.0, 'ACTIVE')
                """, adminMemberId);
        courseMapper.insert(new CourseRecord("course-public", adminMemberId, "공개 코스", "서울", null));
        insertCourseItem("course-public", "ATTRACTION", 1L, null, 1);
        insertCourseItem("course-public", "ATTRACTION", 2L, null, 2);
        insertCourseItem("course-public", "NOTE", null, 1L, 3);
        insertCourseItem("course-public", "NOTE", null, 2L, 4);

        assertThat(courseMapper.findPublicItemsByCourseId("course-public"))
                .extracting(CourseCurationMapperH2Test::itemTitle)
                .containsExactly("공개 장소", "공개 노트");
    }

    @DisplayName("코스 아이템은 next metric을 포함해 batch insert하고 id와 next metric을 반환한다")
    @Test
    void batchInsertCourseItemsWithNextMetrics() {
        Long adminMemberId = seedMember("admin", "admin@example.com");
        seedAttraction(1L, "첫 장소");
        seedAttraction(2L, "두 번째 장소");
        courseMapper.insert(new CourseRecord("course-batch", adminMemberId, "배치 코스", "서울", null));

        CourseItemRecord first = new CourseItemRecord(
                "course-batch", "ATTRACTION", 1L, null, 1, 420, 300
        );
        CourseItemRecord second = new CourseItemRecord(
                "course-batch", "ATTRACTION", 2L, null, 2, null, null
        );

        int inserted = courseMapper.insertItems(List.of(first, second));

        assertThat(inserted).isEqualTo(2);
        assertThat(first.getId()).isNotNull();
        assertThat(second.getId()).isNotNull();

        List<CourseItemDetailRecord> items = courseMapper.findItemsByCourseId("course-batch");

        assertThat(items).hasSize(2);
        assertThat(items.get(0).distanceToNext()).isEqualTo(420);
        assertThat(items.get(0).durationToNext()).isEqualTo(300);
        assertThat(items.get(1).distanceToNext()).isNull();
        assertThat(items.get(1).durationToNext()).isNull();
    }

    @DisplayName("H2 스키마 제약은 음수 next metric을 거부한다")
    @Test
    void schemaConstraintRejectsNegativeNextMetrics() {
        Long adminMemberId = seedMember("admin", "admin@example.com");
        seedAttraction(1L, "장소");
        courseMapper.insert(new CourseRecord("course-constraint", adminMemberId, "제약 테스트", "서울", null));

        assertThatThrownBy(() -> courseMapper.insertItem(new CourseItemRecord(
                "course-constraint", "ATTRACTION", 1L, null, 1, -1, 300
        ))).isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() -> courseMapper.insertItem(new CourseItemRecord(
                "course-constraint", "ATTRACTION", 1L, null, 1, 300, -1
        ))).isInstanceOf(RuntimeException.class);
    }

    @DisplayName("V13 마이그레이션 파일은 next metric 컬럼 추가와 segment 테이블 삭제를 포함한다")
    @Test
    void migrationV13AddsNextMetricColumnsAndDropsSegmentTable() throws Exception {
        try (var inputStream = getClass().getResourceAsStream(
                "/db/migration/V13__simplify_course_schema.sql"
        )) {
            assertThat(inputStream).isNotNull();
            String sql = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

            assertThat(sql).contains("distance_to_next");
            assertThat(sql).contains("duration_to_next");
            assertThat(sql).contains("drop table if exists course_route_segments");
            assertThat(sql).contains("drop column if exists visibility");
            assertThat(sql).contains("drop column if exists status");
        }
    }

    private void insertCourseItem(String courseId,
                                  String itemType,
                                  Long attractionId,
                                  Long noteId,
                                  int position) {
        courseMapper.insertItem(new CourseItemRecord(
                courseId, itemType, attractionId, noteId, position, null, null
        ));
    }

    private static String itemTitle(CourseItemDetailRecord item) {
        if (item.attractionTitle() != null) {
            return item.attractionTitle();
        }
        return item.noteTitle();
    }
}
