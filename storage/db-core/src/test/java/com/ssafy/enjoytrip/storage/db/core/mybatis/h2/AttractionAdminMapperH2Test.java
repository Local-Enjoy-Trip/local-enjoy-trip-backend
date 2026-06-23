package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionAdminRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AttractionAdminMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private AttractionMapper attractionMapper;

    @DisplayName("관리자 장소 목록은 전체 로딩 대신 limit offset으로 20개 단위 페이지를 조회한다")
    @Test
    void adminPlacesUseLimitOffsetPaging() {
        for (long id = 1; id <= 25; id++) {
            seedAdminAttraction(id, "장소 " + id, "ACTIVE");
        }

        assertThat(attractionMapper.countForAdmin(true)).isEqualTo(25);
        assertThat(attractionMapper.findAdminPage(true, 20, 20))
                .extracting(AttractionAdminRecord::id)
                .containsExactly(5L, 4L, 3L, 2L, 1L);
    }

    @DisplayName("관리자 장소 목록은 숨김 제외 옵션에서 숨김 장소를 count와 page 모두에서 제외한다")
    @Test
    void adminPlacesExcludeHiddenWhenRequested() {
        seedAdminAttraction(1L, "공개 장소", "ACTIVE");
        seedAdminAttraction(2L, "숨김 장소", "HIDDEN");

        assertThat(attractionMapper.countForAdmin(false)).isEqualTo(1);
        assertThat(attractionMapper.findAdminPage(false, 20, 0))
                .extracting(AttractionAdminRecord::title)
                .containsExactly("공개 장소");
    }

    private void seedAdminAttraction(Long id, String title, String status) {
        jdbcTemplate.update("""
                insert into attractions (id, title, status, created_at, deleted_at)
                values (?, ?, ?, dateadd('SECOND', ?, timestamp '2026-01-01 00:00:00'), null)
                """, id, title, status, id);
    }
}
