package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.SavedAttractionInputRecord;
import com.ssafy.enjoytrip.storage.db.core.model.SavedNoteInputRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberProfileEmbeddingMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteTagMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.TagMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("postgis")
@Tag("pgvector")
class MemberProfileEmbeddingMapperContainerTest extends StorageContainerTestSupport {

    @Autowired
    private MemberProfileEmbeddingMapper memberProfileEmbeddingMapper;

    @Autowired
    private NoteTagMapper noteTagMapper;

    @Autowired
    private TagMapper tagMapper;

    @DisplayName("findSavedAttractionInputsByMemberId는 회원이 저장한 관광지 목록을 반환한다")
    @Test
    void findSavedAttractionInputsByMemberIdReturnsAttractions() {
        Long memberId = seedMember("여행자", uniqueId("traveler") + "@example.com");
        long attractionId1 = 8800001L;
        long attractionId2 = 8800002L;
        seedAttraction(attractionId1, "경복궁", 1, 1);
        seedAttraction(attractionId2, "남산타워", 1, 1);

        jdbcTemplate.update(
                "insert into attraction_saves (attraction_id, member_id) values (?, ?)",
                attractionId1, memberId
        );
        jdbcTemplate.update(
                "insert into attraction_saves (attraction_id, member_id) values (?, ?)",
                attractionId2, memberId
        );

        List<SavedAttractionInputRecord> results =
                memberProfileEmbeddingMapper.findSavedAttractionInputsByMemberId(memberId);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(SavedAttractionInputRecord::getTitle)
                .containsExactlyInAnyOrder("경복궁", "남산타워");
    }

    @DisplayName("findSavedNoteInputsByMemberId는 저장한 쪽지와 태그를 string_agg로 집계해 반환한다")
    @Test
    void findSavedNoteInputsByMemberIdAggregatesTagsWithStringAgg() {
        Long authorId = seedMember("작성자", uniqueId("author") + "@example.com");
        Long saverId = seedMember("저장자", uniqueId("saver") + "@example.com");

        Long noteId = seedNote(authorId, "북한산 등산", "TIP");
        Long tagId1 = tagMapper.insert(uniqueId("자연")).id();
        Long tagId2 = tagMapper.insert(uniqueId("산")).id();
        noteTagMapper.insertAll(noteId, List.of(tagId1, tagId2));

        jdbcTemplate.update(
                "insert into note_saves (note_id, member_id) values (?, ?)", noteId, saverId
        );

        List<SavedNoteInputRecord> results =
                memberProfileEmbeddingMapper.findSavedNoteInputsByMemberId(saverId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("북한산 등산");
        assertThat(results.get(0).getCategory()).isEqualTo("TIP");
        assertThat(results.get(0).getTagNames()).isNotBlank();
    }

    @DisplayName("upsertEmbedded는 벡터 임베딩을 저장하고 중복 시 갱신한다")
    @Test
    void upsertEmbeddedStoresAndUpdatesVectorEmbedding() {
        Long memberId = seedMember("임베딩회원", uniqueId("embed") + "@example.com");
        String hash = "a".repeat(64);

        memberProfileEmbeddingMapper.upsertEmbedded(
                memberId,
                "한강 산책을 좋아하는 사용자",
                vectorLiteral(1536),
                "v1",
                hash,
                1536,
                "openai",
                "text-embedding-3-small"
        );

        String storedHash = memberProfileEmbeddingMapper.findSourceHashByMemberId(memberId);
        assertThat(storedHash).isEqualTo(hash);

        String updatedHash = "b".repeat(64);
        memberProfileEmbeddingMapper.upsertEmbedded(
                memberId,
                "업데이트된 설명",
                vectorLiteral(1536),
                "v1",
                updatedHash,
                1536,
                "openai",
                "text-embedding-3-small"
        );

        assertThat(memberProfileEmbeddingMapper.findSourceHashByMemberId(memberId))
                .isEqualTo(updatedHash);
    }

    @DisplayName("upsertFailed는 임베딩 실패 상태를 저장하고 중복 시 갱신한다")
    @Test
    void upsertFailedStoresAndUpdatesFailureState() {
        Long memberId = seedMember("실패회원", uniqueId("failed") + "@example.com");
        String hash = "c".repeat(64);

        memberProfileEmbeddingMapper.upsertFailed(
                memberId,
                "v1",
                hash,
                "openai",
                "unknown",
                "CHAT_CLIENT_UNAVAILABLE",
                "ChatClient을 사용할 수 없습니다."
        );

        assertThat(memberProfileEmbeddingMapper.findSourceHashByMemberId(memberId))
                .isNull();
    }

    @DisplayName("findSavedAttractionInputsByMemberId는 삭제된 관광지를 제외한다")
    @Test
    void findSavedAttractionInputsExcludesDeletedAttractions() {
        Long memberId = seedMember("삭제테스트", uniqueId("del") + "@example.com");
        long activeId = 8800101L;
        long deletedId = 8800102L;
        seedAttraction(activeId, "활성 장소", 1, 1);
        seedAttraction(deletedId, "삭제된 장소", 1, 1);
        jdbcTemplate.update(
                "update attractions set deleted_at = current_timestamp where id = ?", deletedId
        );
        jdbcTemplate.update(
                "insert into attraction_saves (attraction_id, member_id) values (?, ?)",
                activeId, memberId
        );
        jdbcTemplate.update(
                "insert into attraction_saves (attraction_id, member_id) values (?, ?)",
                deletedId, memberId
        );

        List<SavedAttractionInputRecord> results =
                memberProfileEmbeddingMapper.findSavedAttractionInputsByMemberId(memberId);

        assertThat(results).extracting(SavedAttractionInputRecord::getTitle)
                .containsOnly("활성 장소");
    }

    private Long seedNote(Long authorMemberId, String title, String category) {
        jdbcTemplate.update("""
                insert into notes (author_member_id, title, content, category, latitude, longitude, location, created_at)
                values (?, ?, '내용', ?, 37.5, 126.9, ST_SetSRID(ST_MakePoint(126.9, 37.5), 4326), current_timestamp)
                """, authorMemberId, title, category);
        return jdbcTemplate.queryForObject(
                "select id from notes where author_member_id = ? and title = ?",
                Long.class,
                authorMemberId,
                title
        );
    }

    private static String vectorLiteral(int dimension) {
        return "[" + "0.001,".repeat(dimension - 1) + "0.001]";
    }
}
