package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AttractionPopularityMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private AttractionMapper attractionMapper;

    @DisplayName("AttractionMapper는 popularity stats favorite_count를 조회한다")
    @Test
    void findPopularityFavoriteCountsReadsStatsTable() {
        seedAttraction(1L, "인기 관광지");
        jdbcTemplate.update("""
                insert into attraction_popularity_stats (attraction_id, favorite_count, updated_at)
                values (?, ?, current_timestamp)
                """, 1L, 7);

        List<AttractionCountRecord> counts = attractionMapper.findPopularityFavoriteCounts(List.of(1L));

        assertThat(counts)
                .extracting(AttractionCountRecord::attractionId, AttractionCountRecord::count)
                .containsExactly(tuple(1L, 7));
    }

    @DisplayName("AttractionMapper는 favorite delta를 0 미만으로 내려가지 않게 반영한다")
    @Test
    void applyPopularityFavoriteDeltaClampsAtZero() {
        seedAttraction(1L, "인기 관광지");

        assertThat(attractionMapper.insertPopularityFavoriteDeltaIfAbsent(1L, 3L)).isEqualTo(1);
        assertThat(attractionMapper.updatePopularityFavoriteDelta(1L, -5L)).isEqualTo(1);

        List<AttractionCountRecord> counts = attractionMapper.findPopularityFavoriteCounts(List.of(1L));

        assertThat(counts).extracting(AttractionCountRecord::count).containsExactly(0);
    }

}
