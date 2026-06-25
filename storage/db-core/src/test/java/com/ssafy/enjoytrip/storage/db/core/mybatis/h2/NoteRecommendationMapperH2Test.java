package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NoteRecommendationMapperH2Test extends H2MapperTestSupport {

    @Autowired
    private NoteMapper noteMapper;

    @DisplayName("findRecentPublic은 PUBLIC이고 삭제되지 않은 쪽지를 최신순으로 반환한다")
    @Test
    void findRecentPublicReturnsPublicNotDeletedNotesByCreatedAtDesc() {
        Long memberId = seedMember("author", "author@example.com");
        seedNote(memberId, "오래된 쪽지", "PUBLIC");
        seedNote(memberId, "최신 쪽지", "PUBLIC");
        seedNote(memberId, "비공개 쪽지", "PRIVATE");

        List<NoteRecord> results = noteMapper.findRecentPublic(10);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(NoteRecord::getTitle)
                .containsExactly("최신 쪽지", "오래된 쪽지");
        assertThat(results).extracting(NoteRecord::getVisibility)
                .containsOnly("PUBLIC");
    }

    @DisplayName("findRecentPublic은 삭제된 쪽지를 반환하지 않는다")
    @Test
    void findRecentPublicExcludesDeletedNotes() {
        Long memberId = seedMember("author2", "author2@example.com");
        seedNote(memberId, "활성 쪽지", "PUBLIC");
        seedDeletedNote(memberId, "삭제된 쪽지", "PUBLIC");

        List<NoteRecord> results = noteMapper.findRecentPublic(10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("활성 쪽지");
    }

    @DisplayName("findRecentPublic은 limit 수만큼만 반환한다")
    @Test
    void findRecentPublicRespectsLimit() {
        Long memberId = seedMember("author3", "author3@example.com");
        seedNote(memberId, "쪽지1", "PUBLIC");
        seedNote(memberId, "쪽지2", "PUBLIC");
        seedNote(memberId, "쪽지3", "PUBLIC");

        List<NoteRecord> results = noteMapper.findRecentPublic(2);

        assertThat(results).hasSize(2);
    }

    private void seedNote(Long authorMemberId, String title, String visibility) {
        jdbcTemplate.update("""
                insert into notes (author_member_id, title, content, visibility, latitude, longitude, status)
                values (?, ?, '내용', ?, 37.5665, 126.9780, 'ACTIVE')
                """, authorMemberId, title, visibility);
    }

    private void seedDeletedNote(Long authorMemberId, String title, String visibility) {
        jdbcTemplate.update("""
                insert into notes (
                    author_member_id, title, content, visibility, latitude, longitude,
                    status, deleted_at
                )
                values (?, ?, '내용', ?, 37.5665, 126.9780, 'DELETED', current_timestamp)
                """, authorMemberId, title, visibility);
    }
}
