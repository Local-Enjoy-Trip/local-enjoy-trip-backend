package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRouteSegmentRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CourseCurationMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private MemberMapper memberMapper;

    @DisplayName("회원 role과 관리자 코스 큐레이션 메타데이터를 저장하고 조회한다")
    @Test
    void storesMemberRoleAndCourseCurationMetadata() {
        seedMember("admin", "admin@example.com");
        jdbcTemplate.update("update members set role = 'ADMIN' where user_id = 'admin'");

        courseMapper.insert(new CourseRecord(
                "course-md-1",
                "admin",
                "서울 산책",
                "서울",
                "PUBLIC",
                "READY",
                "추천 코스",
                "cover.jpg",
                "MD_RECOMMENDED",
                1
        ));

        CourseRecord found = courseMapper.findMdRecommendedPublic(10).get(0);

        assertThat(memberMapper.findByUserId("admin").getRole()).isEqualTo("ADMIN");
        assertThat(found.getId()).isEqualTo("course-md-1");
        assertThat(found.getCurationSection()).isEqualTo("MD_RECOMMENDED");
        assertThat(found.getCurationOrder()).isEqualTo(1);
    }

    @DisplayName("공개 코스 아이템 조회는 숨김 장소와 비공개 노트를 제외한다")
    @Test
    void publicCourseItemsExcludeHiddenPlaceAndPrivateNote() {
        seedMember("admin", "admin@example.com");
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
                    id, author_user_id, title, content, visibility, latitude, longitude, status
                )
                values (1, 'admin', '공개 노트', '내용', 'PUBLIC', 37.5, 127.0, 'ACTIVE')
                """);
        jdbcTemplate.update("""
                insert into notes (
                    id, author_user_id, title, content, visibility, latitude, longitude, status
                )
                values (2, 'admin', '비공개 노트', '내용', 'PRIVATE', 37.5, 127.0, 'ACTIVE')
                """);
        courseMapper.insert(new CourseRecord(
                "course-public",
                "admin",
                "공개 코스",
                "서울",
                "PUBLIC",
                "READY",
                null,
                null,
                null,
                null
        ));
        insertCourseItem("course-public", "ATTRACTION", 1L, null, 1);
        insertCourseItem("course-public", "ATTRACTION", 2L, null, 2);
        insertCourseItem("course-public", "NOTE", null, 1L, 3);
        insertCourseItem("course-public", "NOTE", null, 2L, 4);

        assertThat(courseMapper.findPublicItemsByCourseId("course-public"))
                .extracting(CourseCurationMapperH2Test::itemTitle)
                .containsExactly("공개 장소", "공개 노트");
    }

    @DisplayName("인기 코스는 저장 수 내림차순과 결정적 보조 정렬을 적용한다")
    @Test
    void popularCoursesUseSaveCountDescendingOrder() {
        seedMember("admin", "admin@example.com");
        seedMember("user1", "user1@example.com");
        seedMember("user2", "user2@example.com");
        courseMapper.insert(publicCourse("popular-a"));
        courseMapper.insert(publicCourse("popular-b"));
        jdbcTemplate.update("insert into course_saves (course_id, user_id) values ('popular-b', 'user1')");
        jdbcTemplate.update("insert into course_saves (course_id, user_id) values ('popular-b', 'user2')");
        jdbcTemplate.update("insert into course_saves (course_id, user_id) values ('popular-a', 'user1')");

        assertThat(courseMapper.findPopularPublic(10))
                .extracting(CourseRecord::getId)
                .containsExactly("popular-b", "popular-a");
    }

    @DisplayName("코스 상태 기본값은 READY이고 DRAFT 저장은 거부한다")
    @Test
    void courseStatusDefaultsToReadyAndRejectsDraft() {
        seedMember("admin", "admin@example.com");

        jdbcTemplate.update("""
                insert into courses (id, owner_user_id, title, visibility)
                values ('course-default', 'admin', '기본 상태 코스', 'PUBLIC')
                """);

        String status = jdbcTemplate.queryForObject(
                "select status from courses where id = 'course-default'",
                String.class
        );

        assertThat(status).isEqualTo("READY");
        assertThatThrownBy(() -> courseMapper.insert(new CourseRecord(
                "course-draft",
                "admin",
                "초안 코스",
                "서울",
                "PUBLIC",
                "DRAFT",
                null,
                null,
                null,
                null
        ))).isInstanceOf(RuntimeException.class);
    }

    @DisplayName("V9 마이그레이션은 기존 DRAFT를 READY로 변환한다")
    @Test
    void migrationConvertsExistingDraftRowsToReady() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream(
                "/db/migration/V9__redesign_course_route_segments.sql"
        )) {
            assertThat(inputStream).isNotNull();
            String migrationSql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(migrationSql).contains("set status = 'READY'");
            assertThat(migrationSql).contains("where status = 'DRAFT'");
            assertThat(migrationSql).contains("alter column status set default 'READY'");
            assertThat(migrationSql)
                    .contains("check (status in ('READY', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED'))");
        }
    }

    @DisplayName("코스 아이템과 구간은 batch insert로 저장하고 생성 id를 반환한다")
    @Test
    void batchInsertCourseItemsAndSegmentsAssignsGeneratedIds() {
        seedMember("admin", "admin@example.com");
        seedAttraction(1L, "첫 장소");
        seedAttraction(2L, "두 번째 장소");
        courseMapper.insert(publicCourse("course-batch"));
        CourseItemRecord first = new CourseItemRecord(
                "course-batch",
                "ATTRACTION",
                1L,
                null,
                1,
                1,
                null,
                null
        );
        CourseItemRecord second = new CourseItemRecord(
                "course-batch",
                "ATTRACTION",
                2L,
                null,
                2,
                1,
                null,
                null
        );

        int insertedItems = courseMapper.insertItems(List.of(first, second));
        int insertedSegments = courseMapper.insertSegments(List.of(new CourseRouteSegmentRecord(
                "course-batch",
                first.getId(),
                second.getId(),
                1,
                "WALK",
                300,
                420
        )));

        assertThat(insertedItems).isEqualTo(2);
        assertThat(first.getId()).isNotNull();
        assertThat(second.getId()).isNotNull();
        assertThat(insertedSegments).isEqualTo(1);
        assertThat(courseMapper.findSegmentsByCourseId("course-batch"))
                .extracting(CourseRouteSegmentRecord::getDistanceMeters)
                .containsExactly(420);
    }

    @DisplayName("코스 구간 mapper는 생성된 아이템 id로 저장하고 제약 조건을 검증한다")
    @Test
    void courseRouteSegmentsUseReloadedItemIdsAndConstraints() {
        seedMember("admin", "admin@example.com");
        seedAttraction(1L, "첫 장소");
        seedAttraction(2L, "두 번째 장소");
        seedAttraction(3L, "다른 코스 장소");
        courseMapper.insert(publicCourse("course-segments"));
        courseMapper.insert(publicCourse("course-other"));
        insertCourseItem("course-segments", "ATTRACTION", 1L, null, 1);
        insertCourseItem("course-segments", "ATTRACTION", 2L, null, 2);
        insertCourseItem("course-other", "ATTRACTION", 3L, null, 1);

        Long firstItemId = courseMapper.findItemIdsByCourseId("course-segments").get(0).getId();
        Long secondItemId = courseMapper.findItemIdsByCourseId("course-segments").get(1).getId();
        Long otherCourseItemId = courseMapper.findItemIdsByCourseId("course-other").get(0).getId();

        courseMapper.insertSegment(new CourseRouteSegmentRecord(
                "course-segments",
                firstItemId,
                secondItemId,
                1,
                "WALK",
                300,
                420
        ));

        assertThat(courseMapper.findSegmentsByCourseId("course-segments"))
                .extracting(CourseRouteSegmentRecord::getDistanceMeters)
                .containsExactly(420);
        assertThatThrownBy(() -> courseMapper.insertSegment(new CourseRouteSegmentRecord(
                "course-segments",
                firstItemId,
                secondItemId,
                1,
                "WALK",
                301,
                421
        ))).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> courseMapper.insertSegment(new CourseRouteSegmentRecord(
                "course-segments",
                firstItemId,
                otherCourseItemId,
                2,
                "WALK",
                10,
                10
        ))).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> courseMapper.insertSegment(new CourseRouteSegmentRecord(
                "course-segments",
                firstItemId,
                secondItemId,
                2,
                "WALK",
                -1,
                10
        ))).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> courseMapper.insertSegment(new CourseRouteSegmentRecord(
                "course-segments",
                firstItemId,
                secondItemId,
                2,
                "WALK",
                10,
                -1
        ))).isInstanceOf(RuntimeException.class);
    }
    private void insertCourseItem(String courseId,
                                  String itemType,
                                  Long attractionId,
                                  Long noteId,
                                  int position) {
        courseMapper.insertItem(new CourseItemRecord(
                courseId,
                itemType,
                attractionId,
                noteId,
                position,
                1,
                null,
                null
        ));
    }

    private static String itemTitle(CourseItemDetailRecord item) {
        if (item.attractionTitle() != null) {
            return item.attractionTitle();
        }
        return item.noteTitle();
    }
    private static CourseRecord publicCourse(String id) {
        return new CourseRecord(id, "admin", id, "서울", "PUBLIC", "READY", null, null, null, null);
    }
}
