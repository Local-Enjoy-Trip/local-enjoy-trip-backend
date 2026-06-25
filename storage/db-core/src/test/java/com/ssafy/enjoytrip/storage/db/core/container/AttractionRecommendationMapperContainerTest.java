package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("postgis")
class AttractionRecommendationMapperContainerTest extends StorageContainerTestSupport {

    @Autowired
    private AttractionMapper attractionMapper;

    @DisplayName("findTopBySaveCount는 save_count 내림차순으로 관광지를 반환한다")
    @Test
    void findTopBySaveCountReturnsAttractionsOrderedBySaveCountDesc() {
        long idHigh = 9200001L;
        long idLow = 9200002L;
        long idNone = 9200003L;

        seedAttraction(idHigh, "인기 관광지", 1, 1);
        seedAttraction(idLow, "덜 인기 관광지", 1, 1);
        seedAttraction(idNone, "저장 없는 관광지", 1, 1);
        jdbcTemplate.update("""
                insert into attraction_popularity_stats (attraction_id, save_count, updated_at)
                values (?, 10, current_timestamp),
                       (?, 3, current_timestamp)
                on conflict (attraction_id) do update set save_count = excluded.save_count
                """, idHigh, idLow);

        List<AttractionSearchRecord> results = attractionMapper.findTopBySaveCount(3, null);

        assertThat(results).extracting(AttractionSearchRecord::id)
                .first().isEqualTo(idHigh);
        assertThat(results.get(1).id()).isEqualTo(idLow);
    }

    @DisplayName("findTopBySaveCount는 limit 수만큼만 반환한다")
    @Test
    void findTopBySaveCountRespectsLimit() {
        seedAttraction(9200101L, "관광지A", 1, 1);
        seedAttraction(9200102L, "관광지B", 1, 1);
        seedAttraction(9200103L, "관광지C", 1, 1);

        List<AttractionSearchRecord> results = attractionMapper.findTopBySaveCount(2, null);

        assertThat(results).hasSizeLessThanOrEqualTo(2);
    }

    @DisplayName("findTopBySaveCount에서 viewerMemberId가 있으면 saved 필드를 계산한다")
    @Test
    void findTopBySaveCountCalculatesSavedForViewer() {
        long attractionId = 9200201L;
        seedAttraction(attractionId, "저장된 관광지", 1, 1);
        Long memberId = seedMember("viewer", uniqueId("viewer") + "@example.com");
        jdbcTemplate.update(
                "insert into attraction_saves (attraction_id, member_id) values (?, ?)"
                + " on conflict do nothing",
                attractionId, memberId);

        List<AttractionSearchRecord> results = attractionMapper.findTopBySaveCount(10, memberId);

        assertThat(results).anySatisfy(record -> {
            assertThat(record.id()).isEqualTo(attractionId);
            assertThat(record.saved()).isTrue();
        });
    }
}
