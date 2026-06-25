package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("postgis")
class NoteMapperContainerTest extends StorageContainerTestSupport {
    @Autowired
    private NoteMapper noteMapper;

    @DisplayName("NoteMapper는 migration schema에서 insert/nearby/map-pin/update/delete SQL을 실행한다")
    @Test
    void noteMapperWorksAgainstMigratedPostgisSchema() {
        Long authorMemberId = seedMember("note-author", uniqueId("note-author") + "@example.com");
        NoteRecord saved = noteMapper.insert(new NoteRecord(
                authorMemberId,
                "서비스커넥션 노트",
                "실제 PostgreSQL에서 저장되는 노트",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"),
                "서울 중구",
                "notes/service-connection.png",
                "https://example.com/notes/service-connection.png",
                "image/png"
        ));

        List<NoteRecord> nearby = noteMapper.findNearbyAccessible(
                126.9781,
                37.5666,
                100,
                10,
                null
        );
        List<NoteMapPinRecord> pins = noteMapper.findMapPins(
                126.9781,
                37.5666,
                100,
                10,
                null,
                "TIP",
                false
        );
        NoteRecord updated = noteMapper.updateOwned(new NoteRecord(
                saved.getId(),
                authorMemberId,
                "서비스커넥션 노트 수정",
                "실제 PostgreSQL에서 수정되는 노트",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5667000"),
                new BigDecimal("126.9782000"),
                "서울 중구",
                "notes/service-connection-updated.png",
                "https://example.com/notes/service-connection-updated.png",
                "image/png"
        ));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(nearby).extracting(NoteRecord::getId).contains(saved.getId());
        assertThat(pins).extracting(NoteMapPinRecord::id).contains(saved.getId());
        assertThat(updated.getTitle()).isEqualTo("서비스커넥션 노트 수정");
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(noteMapper.softDeleteOwned(saved.getId(), authorMemberId)).isEqualTo(1);
        assertThat(noteMapper.findById(saved.getId()).getDeletedAt()).isNotNull();
    }
    @DisplayName("NoteMapper는 저장 목록에서 접근 가능한 active 쪽지만 반환한다")
    @Test
    void noteMapperFindsOnlySavedAccessibleActiveNotes() {
        Long viewerMemberId = seedMember("note-save-viewer", uniqueId("note-viewer") + "@example.com");
        Long otherMemberId = seedMember("note-save-other", uniqueId("note-other") + "@example.com");
        NoteRecord selfPrivate = noteMapper.insert(new NoteRecord(
                viewerMemberId,
                "내 비공개 저장 쪽지",
                "내 쪽지는 저장 목록에 보인다",
                "TIP",
                "PRIVATE",
                new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"),
                "서울 중구",
                null,
                null,
                null
        ));
        NoteRecord otherPrivate = noteMapper.insert(new NoteRecord(
                otherMemberId,
                "타인 비공개 저장 쪽지",
                "저장 row가 있어도 보이면 안 된다",
                "TIP",
                "PRIVATE",
                new BigDecimal("37.5666000"),
                new BigDecimal("126.9781000"),
                "서울 중구",
                null,
                null,
                null
        ));
        NoteRecord deleted = noteMapper.insert(new NoteRecord(
                viewerMemberId,
                "삭제된 저장 쪽지",
                "삭제되면 저장 목록에서 제외된다",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5667000"),
                new BigDecimal("126.9782000"),
                "서울 중구",
                null,
                null,
                null
        ));

        assertThat(noteMapper.existsAccessibleActive(selfPrivate.getId(), viewerMemberId)).isEqualTo(1);
        assertThat(noteMapper.existsAccessibleActive(otherPrivate.getId(), viewerMemberId)).isZero();
        noteMapper.insertSave(selfPrivate.getId(), viewerMemberId);
        noteMapper.insertSave(otherPrivate.getId(), viewerMemberId);
        noteMapper.insertSave(deleted.getId(), viewerMemberId);
        noteMapper.softDeleteOwned(deleted.getId(), viewerMemberId);

        List<NoteRecord> saved = noteMapper.findSavedAccessible(viewerMemberId, 10);

        assertThat(saved).extracting(NoteRecord::getId).containsExactly(selfPrivate.getId());
    }

    @DisplayName("NoteMapper는 자신이 작성한 active 쪽지만 반환한다")
    @Test
    void noteMapperFindsOnlyWrittenActiveNotes() {
        Long authorMemberId = seedMember("note-written-author", uniqueId("note-author") + "@example.com");
        Long otherMemberId = seedMember("note-written-other", uniqueId("note-other") + "@example.com");

        NoteRecord myActive = noteMapper.insert(new NoteRecord(
                authorMemberId,
                "내 active 쪽지",
                "내 active 쪽지",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"),
                "서울 중구",
                null,
                null,
                null
        ));
        NoteRecord myDeleted = noteMapper.insert(new NoteRecord(
                authorMemberId,
                "내 deleted 쪽지",
                "내 deleted 쪽지",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"),
                "서울 중구",
                null,
                null,
                null
        ));
        NoteRecord otherActive = noteMapper.insert(new NoteRecord(
                otherMemberId,
                "타인 active 쪽지",
                "타인 active 쪽지",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"),
                "서울 중구",
                null,
                null,
                null
        ));

        noteMapper.softDeleteOwned(myDeleted.getId(), authorMemberId);

        List<NoteRecord> written = noteMapper.findWritten(authorMemberId, 10);

        assertThat(written).extracting(NoteRecord::getId).containsExactly(myActive.getId());
    }

    @DisplayName("searchMapNotes는 키워드 검색을 수행하며 정확히 일치하는 결과가 먼저 나오도록 랭킹과 거리를 정렬하고 개인정보 마스킹을 수행한다")
    @Test
    void searchMapNotesFiltersAndRanksCorrectly() {
        Long viewerMemberId = seedMember("viewer", uniqueId("viewer") + "@example.com");
        Long authorMemberId = seedMember("author", uniqueId("author") + "@example.com");
        Long friendMemberId = seedMember("friend", uniqueId("friend") + "@example.com");

        // 친구 관계 맺기
        jdbcTemplate.update("insert into friendships (requester_member_id, addressee_member_id, status) values (?, ?, 'ACCEPTED')",
                viewerMemberId, friendMemberId);

        // 쪽지 insert
        // 1. EXACT match, PUBLIC, 먼 거리 (약 55m)
        NoteRecord exactPublic = noteMapper.insert(new NoteRecord(
                authorMemberId,
                "EXACT PUBLIC",
                "hello",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.56650"),
                new BigDecimal("126.97850"),
                "서울 중구",
                null,
                null,
                null
        ));

        // 2. CONTAINS match, PUBLIC, 가까운 거리 (약 0m)
        NoteRecord containsPublic = noteMapper.insert(new NoteRecord(
                authorMemberId,
                "CONTAINS PUBLIC",
                "say hello world",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.56650"),
                new BigDecimal("126.97800"),
                "서울 중구",
                null,
                null,
                null
        ));

        // 3. EXACT match, FRIENDS, 중간 거리, 친구 작성 (보여야 함)
        NoteRecord friendFriends = noteMapper.insert(new NoteRecord(
                friendMemberId,
                "FRIEND FRIENDS",
                "hello",
                "TIP",
                "FRIENDS",
                new BigDecimal("37.56650"),
                new BigDecimal("126.97830"),
                "서울 중구",
                null,
                null,
                null
        ));

        // 4. EXACT match, FRIENDS, 비친구 작성 (안 보여야 함)
        NoteRecord nonFriendFriends = noteMapper.insert(new NoteRecord(
                authorMemberId,
                "NON-FRIEND FRIENDS",
                "hello",
                "TIP",
                "FRIENDS",
                new BigDecimal("37.56650"),
                new BigDecimal("126.97840"),
                "서울 중구",
                null,
                null,
                null
        ));

        // 5. EXACT match, PRIVATE, 타인 작성 (안 보여야 함)
        NoteRecord nonSelfPrivate = noteMapper.insert(new NoteRecord(
                authorMemberId,
                "OTHER PRIVATE",
                "hello",
                "TIP",
                "PRIVATE",
                new BigDecimal("37.56650"),
                new BigDecimal("126.97860"),
                "서울 중구",
                null,
                null,
                null
        ));

        // 6. EXACT match, PRIVATE, 가까운 거리, 본인 작성 (보여야 함)
        NoteRecord selfPrivate = noteMapper.insert(new NoteRecord(
                viewerMemberId,
                "SELF PRIVATE",
                "hello",
                "TIP",
                "PRIVATE",
                new BigDecimal("37.56650"),
                new BigDecimal("126.97810"),
                "서울 중구",
                null,
                null,
                null
        ));

        // 7. 와일드카드 이스케이프 확인용
        NoteRecord wildcardNote = noteMapper.insert(new NoteRecord(
                viewerMemberId,
                "WILDCARD",
                "special %_ character",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.56650"),
                new BigDecimal("126.97800"),
                "서울 중구",
                null,
                null,
                null
        ));

        // 일반 검색 검증 (radius는 null)
        List<NoteMapPinRecord> results = noteMapper.searchMapNotes(
                "hello",
                "hello",
                126.97800,
                37.56650,
                null,
                null,
                50,
                viewerMemberId
        );

        // 결과 리스트의 ID 추출
        // exactPublic, friendFriends, selfPrivate가 EXACT match
        // containsPublic는 CONTAINS match
        // 정렬 순서: EXACT match 중 거리가 가까운 순 -> selfPrivate -> friendFriends -> exactPublic -> containsPublic
        // 비친구의 FRIENDS글(nonFriendFriends), 타인의 PRIVATE글(nonSelfPrivate)은 없어야 함.

        assertThat(results).extracting(NoteMapPinRecord::id)
                .containsExactly(selfPrivate.getId(), friendFriends.getId(), exactPublic.getId(), containsPublic.getId());

        // 마스킹/관계(relationship) 검증
        // 1. selfPrivate -> relationship = SELF
        NoteMapPinRecord selfPin = results.stream().filter(r -> r.id().equals(selfPrivate.getId())).findFirst().orElseThrow();
        assertThat(selfPin.relationship()).isEqualTo("SELF");

        // 2. friendFriends -> relationship = FRIEND
        NoteMapPinRecord friendPin = results.stream().filter(r -> r.id().equals(friendFriends.getId())).findFirst().orElseThrow();
        assertThat(friendPin.relationship()).isEqualTo("FRIEND");

        // 3. exactPublic -> relationship = NONE (타인)
        NoteMapPinRecord publicPin = results.stream().filter(r -> r.id().equals(exactPublic.getId())).findFirst().orElseThrow();
        assertThat(publicPin.relationship()).isEqualTo("NONE");

        // 와일드카드 이스케이프 검증 (%, _ 와일드카드가 이스케이프되어 raw '%' 문자만 매칭)
        List<NoteMapPinRecord> wildcardResults = noteMapper.searchMapNotes(
                "%",
                "\\%",
                126.97800,
                37.56650,
                null,
                null,
                50,
                viewerMemberId
        );
        assertThat(wildcardResults).extracting(NoteMapPinRecord::id)
                .containsExactly(wildcardNote.getId());
    }
}
