package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NoteMapperContainerTest extends StorageContainerTestSupport {
    @Autowired
    private NoteMapper noteMapper;

    @DisplayName("NoteMapper는 migration schema에서 insert/nearby/map-pin/update/delete SQL을 실행한다")
    @Test
    void noteMapperWorksAgainstMigratedPostgisSchema() {
        String author = uniqueId("note-author");
        seedMember(author, author + "@example.com");
        NoteRecord saved = noteMapper.insert(new NoteRecord(
                author,
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
                author,
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
        assertThat(noteMapper.softDeleteOwned(saved.getId(), author)).isEqualTo(1);
        assertThat(noteMapper.findById(saved.getId()).getDeletedAt()).isNotNull();
    }
}
