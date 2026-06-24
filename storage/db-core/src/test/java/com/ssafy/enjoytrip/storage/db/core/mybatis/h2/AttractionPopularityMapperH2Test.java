package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionPopularityDeltaRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionStatsRowRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AttractionPopularityMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private AttractionMapper attractionMapper;

    @DisplayName("AttractionMapper는 popularity stats save_count를 조회한다")
    @Test
    void findPopularityCountsReadsStatsTable() {
        seedAttraction(1L, "인기 관광지");
        jdbcTemplate.update("""
                insert into attraction_popularity_stats (
                    attraction_id,
                    save_count,
                    rating_count,
                    average_rating,
                    updated_at
                )
                values (?, ?, ?, ?, current_timestamp)
                """, 1L, 5, 2, 4.5);

        List<AttractionCountRecord> counts = attractionMapper.findPopularityCounts(List.of(1L));

        assertThat(counts)
                .extracting(AttractionCountRecord::attractionId, AttractionCountRecord::count)
                .containsExactly(tuple(1L, 5));
    }

    @DisplayName("AttractionMapper는 통계와 회원 상태를 단일 join row로 조회한다")
    @Test
    void findStatsRowsReadsStatsUserStateAndTagsWithSingleJoinProjection() {
        seedAttraction(1L, "저장 관광지");
        seedAttraction(2L, "다른 저장 관광지");
        Long memberId = seedMember("ssafy", "ssafy@example.com");
        Long otherMemberId = seedMember("other", "other@example.com");
        insertAttractionSave(1L, memberId);
        insertAttractionSave(1L, otherMemberId);
        insertAttractionSave(2L, memberId);
        insertAttractionRating(1L, memberId, 5);
        jdbcTemplate.update("""
                insert into attraction_popularity_stats (
                    attraction_id,
                    save_count,
                    rating_count,
                    average_rating,
                    updated_at
                )
                values (?, ?, ?, ?, current_timestamp),
                       (?, ?, ?, ?, current_timestamp)
                """, 1L, 7, 3, 4.3, 2L, 3, 1, 2.5);

        List<AttractionStatsRowRecord> rows =
                attractionMapper.findStatsRowsByAttractionIds(List.of(1L, 2L), memberId);

        assertThat(rows)
                .extracting(
                        AttractionStatsRowRecord::attractionId,
                        AttractionStatsRowRecord::saveCount,
                        AttractionStatsRowRecord::ratingCount,
                        AttractionStatsRowRecord::averageRating,
                        AttractionStatsRowRecord::saved,
                        AttractionStatsRowRecord::myRating
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, 7, 3, 4.3, true, 5),
                        tuple(2L, 3, 1, 2.5, true, null)
                );
    }

    @DisplayName("AttractionMapper는 여러 popularity delta를 하나의 batch upsert SQL로 반영한다")
    @Test
    void applyPopularityDeltasUsesBatchUpsert() {
        seedAttraction(1L, "인기 관광지");
        seedAttraction(2L, "저장 관광지");
        jdbcTemplate.update("""
                insert into attraction_popularity_stats (
                    attraction_id,
                    save_count,
                    updated_at
                )
                values (?, ?, current_timestamp),
                       (?, ?, current_timestamp)
                """, 1L, 2, 2L, 5);

        int saveUpdates = attractionMapper.applyPopularitySaveDeltas(List.of(
                new AttractionPopularityDeltaRecord(1L, -9),
                new AttractionPopularityDeltaRecord(2L, 3)
        ));

        assertThat(saveUpdates).isPositive();
        assertThat(attractionMapper.findStatsRowsByAttractionIds(List.of(1L, 2L), null))
                .extracting(
                        AttractionStatsRowRecord::attractionId,
                        AttractionStatsRowRecord::saveCount
                )
                .containsExactlyInAnyOrder(tuple(1L, 0), tuple(2L, 8));
    }

    private void insertAttractionSave(Long attractionId, Long memberId) {
        jdbcTemplate.update("""
                insert into attraction_saves (attraction_id, member_id)
                values (?, ?)
                """, attractionId, memberId);
    }

    private void insertAttractionRating(Long attractionId, Long memberId, int rating) {
        jdbcTemplate.update("""
                insert into attraction_ratings (attraction_id, member_id, rating)
                values (?, ?, ?)
                """, attractionId, memberId, rating);
    }

}
