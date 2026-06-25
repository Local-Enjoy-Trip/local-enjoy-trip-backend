package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.NoteTagRecord;
import com.ssafy.enjoytrip.storage.db.core.model.TagFrequencyRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteTagMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NoteTagMapperH2Test extends H2MapperTestSupport {

    @Autowired
    private NoteTagMapper noteTagMapper;

    @DisplayName("findByNoteId는 쪽지에 연결된 태그 목록을 반환한다")
    @Test
    void findByNoteIdReturnsTagsForNote() {
        Long memberId = seedMember("작성자", "writer@example.com");
        Long noteId = seedNote(memberId, "한강 산책");
        Long tagId1 = seedTag("자연");
        Long tagId2 = seedTag("산책");
        seedNoteTag(noteId, tagId1);
        seedNoteTag(noteId, tagId2);

        List<NoteTagRecord> tags = noteTagMapper.findByNoteId(noteId);

        assertThat(tags).hasSize(2);
        assertThat(tags).extracting(NoteTagRecord::getTagName)
                .containsExactlyInAnyOrder("자연", "산책");
    }

    @DisplayName("deleteByNoteId는 해당 쪽지의 모든 태그 연결을 삭제한다")
    @Test
    void deleteByNoteIdRemovesAllTagLinks() {
        Long memberId = seedMember("작성자", "writer2@example.com");
        Long noteId = seedNote(memberId, "북한산 등산");
        Long tagId = seedTag("등산");
        seedNoteTag(noteId, tagId);

        noteTagMapper.deleteByNoteId(noteId);

        assertThat(noteTagMapper.findByNoteId(noteId)).isEmpty();
    }

    @DisplayName("findByNoteId는 다른 쪽지의 태그를 포함하지 않는다")
    @Test
    void findByNoteIdReturnsOnlyTagsForTargetNote() {
        Long memberId = seedMember("작성자", "writer3@example.com");
        Long noteId1 = seedNote(memberId, "쪽지1");
        Long noteId2 = seedNote(memberId, "쪽지2");
        Long tagId = seedTag("등산");
        seedNoteTag(noteId2, tagId);

        List<NoteTagRecord> tags = noteTagMapper.findByNoteId(noteId1);

        assertThat(tags).isEmpty();
    }

    @DisplayName("findTagFrequencyByMemberId는 회원이 저장한 쪽지의 태그별 빈도를 반환한다")
    @Test
    void findTagFrequencyByMemberIdReturnsTagCountsForSavedNotes() {
        Long memberId = seedMember("저장자", "saver@example.com");
        Long otherMemberId = seedMember("다른사람", "other@example.com");

        Long noteId1 = seedNote(otherMemberId, "쪽지1");
        Long noteId2 = seedNote(otherMemberId, "쪽지2");
        Long noteId3 = seedNote(otherMemberId, "쪽지3");

        Long tagNature = seedTag("자연");
        Long tagWalk = seedTag("산책");
        Long tagMountain = seedTag("등산");

        seedNoteTag(noteId1, tagNature);
        seedNoteTag(noteId1, tagWalk);
        seedNoteTag(noteId2, tagNature);
        seedNoteTag(noteId3, tagMountain);

        jdbcTemplate.update(
                "insert into note_saves (note_id, member_id) values (?, ?)", noteId1, memberId
        );
        jdbcTemplate.update(
                "insert into note_saves (note_id, member_id) values (?, ?)", noteId2, memberId
        );

        List<TagFrequencyRecord> frequency = noteTagMapper.findTagFrequencyByMemberId(memberId);

        Map<Long, Long> frequencyMap = frequency.stream()
                .collect(Collectors.toMap(TagFrequencyRecord::getTagId, TagFrequencyRecord::getCount));

        assertThat(frequencyMap).containsEntry(tagNature, 2L);
        assertThat(frequencyMap).containsEntry(tagWalk, 1L);
        assertThat(frequencyMap).doesNotContainKey(tagMountain);
    }

    @DisplayName("findTagFrequencyByMemberId는 저장한 쪽지가 없으면 빈 목록을 반환한다")
    @Test
    void findTagFrequencyByMemberIdReturnsEmptyWhenNoSavedNotes() {
        Long memberId = seedMember("신규회원", "new@example.com");

        List<TagFrequencyRecord> frequency = noteTagMapper.findTagFrequencyByMemberId(memberId);

        assertThat(frequency).isEmpty();
    }

    private Long seedTag(String name) {
        jdbcTemplate.update("insert into tags (name) values (?)", name);
        return jdbcTemplate.queryForObject(
                "select max(id) from tags where name = ?", Long.class, name
        );
    }

    private void seedNoteTag(Long noteId, Long tagId) {
        jdbcTemplate.update(
                "insert into note_tags (note_id, tag_id) values (?, ?)", noteId, tagId
        );
    }

    private Long seedNote(Long authorMemberId, String title) {
        jdbcTemplate.update("""
                insert into notes (author_member_id, title, content, latitude, longitude, created_at)
                values (?, ?, '내용', 37.5, 126.9, current_timestamp)
                """, authorMemberId, title);
        return jdbcTemplate.queryForObject(
                "select max(id) from notes where author_member_id = ? and title = ?",
                Long.class,
                authorMemberId,
                title
        );
    }
}
