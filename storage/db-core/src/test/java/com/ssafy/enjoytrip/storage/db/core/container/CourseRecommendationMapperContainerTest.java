package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecommendationCandidateRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseTagRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("pgvector")
class CourseRecommendationMapperContainerTest extends StorageContainerTestSupport {

    @Autowired
    private CourseMapper courseMapper;

    @DisplayName("hasMemberProfileEmbedding은 EMBEDDED 상태의 임베딩이 있으면 true를 반환한다")
    @Test
    void hasMemberProfileEmbeddingReturnsTrueWhenEmbeddingExists() {
        Long memberId = seedMember("임베딩회원", uniqueId("embed") + "@test.com");
        seedMemberEmbedding(memberId, vectorLiteral(1536), "EMBEDDED");

        assertThat(courseMapper.hasMemberProfileEmbedding(memberId)).isTrue();
    }

    @DisplayName("hasMemberProfileEmbedding은 임베딩이 없으면 false를 반환한다")
    @Test
    void hasMemberProfileEmbeddingReturnsFalseWhenNoEmbedding() {
        Long memberId = seedMember("미임베딩", uniqueId("noembed") + "@test.com");

        assertThat(courseMapper.hasMemberProfileEmbedding(memberId)).isFalse();
    }

    @DisplayName("hasMemberProfileEmbedding은 FAILED 상태는 false로 처리한다")
    @Test
    void hasMemberProfileEmbeddingReturnsFalseForFailedStatus() {
        Long memberId = seedMember("실패회원", uniqueId("failed") + "@test.com");
        seedMemberEmbedding(memberId, null, "FAILED");

        assertThat(courseMapper.hasMemberProfileEmbedding(memberId)).isFalse();
    }

    @DisplayName("insertView는 코스 조회 이력을 저장한다")
    @Test
    void insertViewSavesCourseViewRecord() {
        Long memberId = seedMember("뷰어", uniqueId("viewer") + "@test.com");
        String courseId = seedCourse(memberId, "한강 코스");

        courseMapper.insertView(courseId, memberId);

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from course_views where course_id = ? and member_id = ?",
                Integer.class,
                courseId,
                memberId
        );
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("findRecentlyViewedCourseIds는 지정 일수 이내 조회한 코스 ID를 반환한다")
    @Test
    void findRecentlyViewedCourseIdsReturnsRecentViews() {
        Long memberId = seedMember("리뷰어", uniqueId("reviewer") + "@test.com");
        String recentCourseId = seedCourse(memberId, "최근 코스");
        String oldCourseId = seedCourse(memberId, "오래된 코스");

        courseMapper.insertView(recentCourseId, memberId);
        jdbcTemplate.update("""
                insert into course_views (course_id, member_id, viewed_at)
                values (?, ?, current_timestamp - interval '10 days')
                """, oldCourseId, memberId);

        List<String> result = courseMapper.findRecentlyViewedCourseIds(memberId, 7);

        assertThat(result).contains(recentCourseId);
        assertThat(result).doesNotContain(oldCourseId);
    }

    @DisplayName("findRecentlyViewedCourseIds는 다른 회원의 조회 이력을 포함하지 않는다")
    @Test
    void findRecentlyViewedCourseIdsExcludesOtherMembersViews() {
        Long memberId = seedMember("회원A", uniqueId("memberA") + "@test.com");
        Long otherMemberId = seedMember("회원B", uniqueId("memberB") + "@test.com");
        String courseId = seedCourse(memberId, "공유 코스");

        courseMapper.insertView(courseId, otherMemberId);

        List<String> result = courseMapper.findRecentlyViewedCourseIds(memberId, 7);

        assertThat(result).doesNotContain(courseId);
    }

    @DisplayName("findCandidatesByMemberProfile은 멤버 임베딩과 코스 임베딩 간 벡터 거리로 후보를 반환한다")
    @Test
    void findCandidatesByMemberProfileReturnsCandidatesOrderedByVectorDistance() {
        Long memberId = seedMember("검색자", uniqueId("searcher") + "@test.com");
        String nearCourseId = seedCourse(memberId, "가까운 코스");
        String farCourseId = seedCourse(memberId, "먼 코스");

        seedMemberEmbedding(memberId, vectorLiteral(1536), "EMBEDDED");
        seedCourseEmbedding(nearCourseId, vectorLiteral(1536), "EMBEDDED");
        seedCourseEmbedding(farCourseId, divergentVectorLiteral(1536), "EMBEDDED");

        List<CourseRecommendationCandidateRecord> candidates =
                courseMapper.findCandidatesByMemberProfile(memberId, 10);

        assertThat(candidates).isNotEmpty();
        List<String> ids = candidates.stream()
                .map(CourseRecommendationCandidateRecord::getId)
                .toList();
        assertThat(ids).contains(nearCourseId, farCourseId);

        int nearIdx = ids.indexOf(nearCourseId);
        int farIdx = ids.indexOf(farCourseId);
        assertThat(nearIdx).isLessThan(farIdx);
    }

    @DisplayName("findCandidatesByMemberProfile은 임베딩이 없는 코스를 제외한다")
    @Test
    void findCandidatesByMemberProfileExcludesCourseWithoutEmbedding() {
        Long memberId = seedMember("필터회원", uniqueId("filter") + "@test.com");
        String embeddedCourseId = seedCourse(memberId, "임베딩 있는 코스");
        String noEmbedCourseId = seedCourse(memberId, "임베딩 없는 코스");

        seedMemberEmbedding(memberId, vectorLiteral(1536), "EMBEDDED");
        seedCourseEmbedding(embeddedCourseId, vectorLiteral(1536), "EMBEDDED");

        List<CourseRecommendationCandidateRecord> candidates =
                courseMapper.findCandidatesByMemberProfile(memberId, 10);

        List<String> ids = candidates.stream()
                .map(CourseRecommendationCandidateRecord::getId)
                .toList();
        assertThat(ids).contains(embeddedCourseId);
        assertThat(ids).doesNotContain(noEmbedCourseId);
    }

    @DisplayName("findPublicItemsByCourseIds는 여러 코스의 아이템을 배치로 조회한다")
    @Test
    void findPublicItemsByCourseIdsReturnsBatchedItems() {
        Long memberId = seedMember("배치회원", uniqueId("batch") + "@test.com");
        String course1Id = seedCourse(memberId, "코스1");
        String course2Id = seedCourse(memberId, "코스2");
        long attractionId = 9900001L;
        seedAttraction(attractionId, "남산타워", 1, 1);

        jdbcTemplate.update("""
                insert into course_items (course_id, item_type, attraction_id, position)
                values (?, 'ATTRACTION', ?, 1)
                """, course1Id, attractionId);
        jdbcTemplate.update("""
                insert into course_items (course_id, item_type, attraction_id, position)
                values (?, 'ATTRACTION', ?, 1)
                """, course2Id, attractionId);

        List<CourseItemDetailRecord> items =
                courseMapper.findPublicItemsByCourseIds(List.of(course1Id, course2Id));

        Map<String, List<CourseItemDetailRecord>> byCourse = items.stream()
                .collect(Collectors.groupingBy(CourseItemDetailRecord::courseId));
        assertThat(byCourse).containsKeys(course1Id, course2Id);
    }

    @DisplayName("findTagsByCourseIds는 여러 코스의 태그를 배치로 조회한다")
    @Test
    void findTagsByCourseIdsReturnsBatchedTags() {
        Long memberId = seedMember("태그배치", uniqueId("tagbatch") + "@test.com");
        String course1Id = seedCourse(memberId, "태그코스1");
        String course2Id = seedCourse(memberId, "태그코스2");

        Long tagId = jdbcTemplate.queryForObject(
                "insert into tags (name) values (?) returning id",
                Long.class,
                uniqueId("자연")
        );
        jdbcTemplate.update(
                "insert into course_tags (course_id, tag_id) values (?, ?)", course1Id, tagId
        );
        jdbcTemplate.update(
                "insert into course_tags (course_id, tag_id) values (?, ?)", course2Id, tagId
        );

        List<CourseTagRecord> tags =
                courseMapper.findTagsByCourseIds(List.of(course1Id, course2Id));

        Map<String, List<CourseTagRecord>> byCourse = tags.stream()
                .collect(Collectors.groupingBy(CourseTagRecord::courseId));
        assertThat(byCourse).containsKeys(course1Id, course2Id);
    }

    @DisplayName("findPublicItemsByCourseIds는 삭제된 코스를 제외한다")
    @Test
    void findPublicItemsByCourseIdsSkipsDeletedCourseItems() {
        Long memberId = seedMember("삭제코스", uniqueId("delcourse") + "@test.com");
        String activeId = seedCourse(memberId, "활성 코스");
        String deletedId = seedCourse(memberId, "삭제 코스");
        long attractionId = 9900002L;
        seedAttraction(attractionId, "관광지", 1, 1);

        jdbcTemplate.update("""
                insert into course_items (course_id, item_type, attraction_id, position)
                values (?, 'ATTRACTION', ?, 1)
                """, activeId, attractionId);
        jdbcTemplate.update("""
                insert into course_items (course_id, item_type, attraction_id, position)
                values (?, 'ATTRACTION', ?, 1)
                """, deletedId, attractionId);
        jdbcTemplate.update(
                "update courses set deleted_at = current_timestamp where id = ?", deletedId
        );

        List<CourseItemDetailRecord> items =
                courseMapper.findPublicItemsByCourseIds(List.of(activeId, deletedId));

        List<String> courseIds = items.stream()
                .map(CourseItemDetailRecord::courseId)
                .distinct()
                .toList();
        assertThat(courseIds).contains(activeId);
    }

    private String seedCourse(Long ownerMemberId, String title) {
        String id = uniqueId("course");
        jdbcTemplate.update("""
                insert into courses (id, owner_member_id, title, region_name, created_at, updated_at)
                values (?, ?, ?, '서울', current_timestamp, current_timestamp)
                """, id, ownerMemberId, title);
        return id;
    }

    private void seedMemberEmbedding(Long memberId, String vectorLiteral, String status) {
        if (vectorLiteral != null) {
            jdbcTemplate.update("""
                    insert into member_profile_embeddings
                        (member_id, profile_description, embedding, source_version, source_hash,
                         embedding_dimension, provider, model, status)
                    values (?, ?, ?::vector, 'v1', 'hash', 1536, 'openai', 'text-embedding-3-small', ?)
                    on conflict (member_id) do update
                        set embedding = excluded.embedding, status = excluded.status
                    """, memberId, "설명", vectorLiteral, status);
        } else {
            jdbcTemplate.update("""
                    insert into member_profile_embeddings
                        (member_id, source_version, source_hash, provider, model,
                         embedding_dimension, status, failure_code, failure_message)
                    values (?, 'v1', 'hash', 'openai', 'text-embedding-3-small', 1536, ?, 'ERR', '실패')
                    on conflict (member_id) do update set status = excluded.status
                    """, memberId, status);
        }
    }

    private void seedCourseEmbedding(String courseId, String vectorLiteral, String status) {
        jdbcTemplate.update("""
                insert into course_embeddings
                    (course_id, description, embedding, source_version, source_hash,
                     embedding_dimension, provider, model, status)
                values (?, ?, ?::vector, 'v1', 'hash', 1536, 'openai', 'text-embedding-3-small', ?)
                on conflict (course_id) do update
                    set embedding = excluded.embedding, status = excluded.status
                """, courseId, "설명", vectorLiteral, status);
    }

    private static String vectorLiteral(int dimension) {
        return "[" + "0.001,".repeat(dimension - 1) + "0.001]";
    }

    private static String divergentVectorLiteral(int dimension) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < dimension; i++) {
            sb.append(i == 0 ? "0.999" : "0.0");
            if (i < dimension - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }
}
