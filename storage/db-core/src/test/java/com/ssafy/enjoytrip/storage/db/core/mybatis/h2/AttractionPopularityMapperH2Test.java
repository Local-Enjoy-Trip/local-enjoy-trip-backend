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

    @DisplayName("AttractionMapper는 popularity stats favorite_count와 save_count 합산를 조회한다")
    @Test
    void findPopularityCountsReadsStatsTable() {
        seedAttraction(1L, "인기 관광지");
        jdbcTemplate.update("""
                insert into attraction_popularity_stats (
                    attraction_id,
                    favorite_count,
                    save_count,
                    rating_count,
                    average_rating,
                    updated_at
                )
                values (?, ?, ?, ?, ?, current_timestamp)
                """, 1L, 7, 5, 2, 4.5);

        List<AttractionCountRecord> counts = attractionMapper.findPopularityCounts(List.of(1L));

        assertThat(counts)
                .extracting(AttractionCountRecord::attractionId, AttractionCountRecord::count)
                .containsExactly(tuple(1L, 12));
    }

    @DisplayName("AttractionMapper는 통계와 사용자 상태와 태그를 단일 join row로 조회한다")
    @Test
    void findStatsRowsReadsStatsUserStateAndTagsWithSingleJoinProjection() {
        seedAttraction(1L, "저장 관광지");
        seedAttraction(2L, "다른 저장 관광지");
        seedMember("ssafy", "ssafy@example.com");
        seedMember("other", "other@example.com");
        insertAttractionFavorite(1L, "ssafy");
        insertAttractionSave(1L, "ssafy");
        insertAttractionSave(1L, "other");
        insertAttractionSave(2L, "ssafy");
        insertAttractionRating(1L, "ssafy", 5);
        insertAttractionTag(10L, "가족");
        insertAttractionTagMapping(1L, 10L);
        jdbcTemplate.update("""
                insert into attraction_popularity_stats (
                    attraction_id,
                    favorite_count,
                    save_count,
                    rating_count,
                    average_rating,
                    updated_at
                )
                values (?, ?, ?, ?, ?, current_timestamp),
                       (?, ?, ?, ?, ?, current_timestamp)
                """, 1L, 9, 7, 3, 4.3, 2L, 4, 3, 1, 2.5);

        List<AttractionStatsRowRecord> rows =
                attractionMapper.findStatsRowsByAttractionIds(List.of(1L, 2L), "ssafy");

        assertThat(rows)
                .extracting(
                        AttractionStatsRowRecord::attractionId,
                        AttractionStatsRowRecord::favoriteCount,
                        AttractionStatsRowRecord::saveCount,
                        AttractionStatsRowRecord::ratingCount,
                        AttractionStatsRowRecord::averageRating,
                        AttractionStatsRowRecord::tagId,
                        AttractionStatsRowRecord::tagName,
                        AttractionStatsRowRecord::favorited,
                        AttractionStatsRowRecord::saved,
                        AttractionStatsRowRecord::myRating
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, 9, 7, 3, 4.3, 10L, "가족", true, true, 5),
                        tuple(2L, 4, 3, 1, 2.5, null, null, false, true, null)
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
                    favorite_count,
                    save_count,
                    updated_at
                )
                values (?, ?, ?, current_timestamp),
                       (?, ?, ?, current_timestamp)
                """, 1L, 3, 2, 2L, 1, 5);

        int favoriteUpdates = attractionMapper.applyPopularityFavoriteDeltas(List.of(
                new AttractionPopularityDeltaRecord(1L, 4),
                new AttractionPopularityDeltaRecord(2L, -5)
        ));
        int saveUpdates = attractionMapper.applyPopularitySaveDeltas(List.of(
                new AttractionPopularityDeltaRecord(1L, -9),
                new AttractionPopularityDeltaRecord(2L, 3)
        ));

        assertThat(favoriteUpdates).isPositive();
        assertThat(saveUpdates).isPositive();
        assertThat(attractionMapper.findStatsRowsByAttractionIds(List.of(1L, 2L), null))
                .extracting(
                        AttractionStatsRowRecord::attractionId,
                        AttractionStatsRowRecord::favoriteCount,
                        AttractionStatsRowRecord::saveCount
                )
                .containsExactlyInAnyOrder(tuple(1L, 7, 0), tuple(2L, 0, 8));
    }

    private void insertAttractionSave(Long attractionId, String userId) {
        jdbcTemplate.update("""
                insert into attraction_saves (attraction_id, user_id)
                values (?, ?)
                """, attractionId, userId);
    }

    private void insertAttractionFavorite(Long attractionId, String userId) {
        jdbcTemplate.update("""
                insert into attraction_favorites (attraction_id, user_id)
                values (?, ?)
                """, attractionId, userId);
    }

    private void insertAttractionRating(Long attractionId, String userId, int rating) {
        jdbcTemplate.update("""
                insert into attraction_ratings (attraction_id, user_id, rating)
                values (?, ?, ?)
                """, attractionId, userId, rating);
    }

    private void insertAttractionTag(Long tagId, String name) {
        jdbcTemplate.update("""
                insert into attraction_tags (id, name)
                values (?, ?)
                """, tagId, name);
    }

    private void insertAttractionTagMapping(Long attractionId, Long tagId) {
        jdbcTemplate.update("""
                insert into attraction_tag_mappings (attraction_id, tag_id)
                values (?, ?)
                """, attractionId, tagId);
    }
}
