package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionEmbeddingSourceRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionStatsRowRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionTagRecord;
import com.ssafy.enjoytrip.storage.db.core.model.ChargerItemRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionEmbeddingMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionEmbeddingMapper.TargetRegionRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.EvChargerMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("postgis")
@Tag("pgvector")
class SpatialVectorMapperContainerTest extends StorageContainerTestSupport {
    @Autowired
    private AttractionMapper attractionMapper;

    @Autowired
    private AttractionEmbeddingMapper attractionEmbeddingMapper;

    @Autowired
    private EvChargerMapper evChargerMapper;

    @DisplayName("AttractionMapper는 검색, 주변 검색, 태그, 좋아요, 평점 SQL을 실행한다")
    @Test
    void attractionMapperRunsSearchAndUserInteractionQueries() {
        long attractionId = 9200001L;
        String userId = uniqueId("attraction-user");
        seedAttraction(attractionId, "서비스커넥션 궁", 1, 1);
        jdbcTemplate.update("""
                insert into attraction_popularity_stats (attraction_id, favorite_count)
                values (?, 7)
                on conflict (attraction_id) do update set favorite_count = excluded.favorite_count
                """, attractionId);

        AttractionTagRecord tag = attractionMapper.insertTag(uniqueId("tag"));
        attractionMapper.insertTagMapping(attractionId, tag.id());
        attractionMapper.insertFavorite(attractionId, userId);
        attractionMapper.upsertRating(attractionId, userId, 5);
        attractionMapper.refreshPopularityRatingStats(attractionId);

        List<AttractionSearchRecord> searched = attractionMapper.search(
                "12",
                "서비스커넥션",
                1,
                1,
                null,
                null,
                null,
                false,
                10,
                userId
        );
        List<AttractionSearchRecord> nearby = attractionMapper.findNearby(
                126.9781,
                37.5666,
                100,
                10,
                false,
                null
        );
        List<AttractionStatsRowRecord> statsRows =
                attractionMapper.findStatsRowsByAttractionIds(List.of(attractionId), userId);
        List<AttractionCountRecord> popularityCounts =
                attractionMapper.findPopularityCounts(List.of(attractionId));

        assertThat(attractionMapper.existsById(attractionId)).isEqualTo(1);
        assertThat(searched).extracting(AttractionSearchRecord::id).contains(attractionId);
        assertThat(searched).extracting(AttractionSearchRecord::tagId).contains(tag.id());
        assertThat(searched).extracting(AttractionSearchRecord::favorited).contains(true);
        assertThat(searched).extracting(AttractionSearchRecord::favoriteCount).contains(7);
        assertThat(searched).extracting(AttractionSearchRecord::ratingCount).contains(1);
        assertThat(searched).extracting(AttractionSearchRecord::ratingAverage).contains(5.0);
        assertThat(searched).extracting(AttractionSearchRecord::myRating).contains(5);
        assertThat(nearby).extracting(AttractionSearchRecord::id).contains(attractionId);
        assertThat(attractionMapper.findByIds(List.of(attractionId))).hasSize(1);
        assertThat(attractionMapper.countTagsByIds(List.of(tag.id()))).isEqualTo(1);
        assertThat(attractionMapper.findAllTags()).extracting(AttractionTagRecord::id).contains(tag.id());
        assertThat(statsRows).extracting(AttractionStatsRowRecord::tagId).contains(tag.id());
        assertThat(statsRows).extracting(AttractionStatsRowRecord::favorited).contains(true);
        assertThat(statsRows).extracting(AttractionStatsRowRecord::favoriteCount).contains(7);
        assertThat(statsRows).extracting(AttractionStatsRowRecord::ratingCount).contains(1);
        assertThat(statsRows).extracting(AttractionStatsRowRecord::averageRating).contains(5.0);
        assertThat(statsRows).extracting(AttractionStatsRowRecord::myRating).contains(5);
        assertThat(popularityCounts).extracting(AttractionCountRecord::count).contains(7);
        assertThat(attractionMapper.deleteRating(attractionId, userId)).isEqualTo(1);
        assertThat(attractionMapper.deleteFavorite(attractionId, userId)).isEqualTo(1);
        assertThat(attractionMapper.deleteTagMappings(attractionId)).isEqualTo(1);
        assertThat(attractionMapper.deleteTag(tag.id())).isEqualTo(1);
    }

    @DisplayName("EvChargerMapper는 PostGIS 위치 컬럼을 포함해 충전소를 조건 검색한다")
    @Test
    void evChargerMapperReadsPostgisLocationRows() {
        jdbcTemplate.update("""
                insert into ev_chargers (
                    stat_id,
                    stat_nm,
                    chger_id,
                    chger_type,
                    addr,
                    location_desc,
                    use_time,
                    busi_nm,
                    busi_call,
                    stat,
                    location
                )
                values (
                    ?,
                    '서비스커넥션 충전소',
                    '01',
                    'FAST',
                    '서울 중구',
                    '시청 앞',
                    '24H',
                    '사업자',
                    '02',
                    'AVAILABLE',
                    ST_SetSRID(ST_MakePoint(126.9780, 37.5665), 4326)
                )
                """, uniqueId("charger"));

        List<ChargerItemRecord> chargers = evChargerMapper.findChargers("서울", "서비스커넥션", 10, 0);

        assertThat(chargers).extracting(ChargerItemRecord::statNm).contains("서비스커넥션 충전소");
    }

    @DisplayName("AttractionEmbeddingMapper는 대상 조회와 임베딩 upsert 상태 SQL을 실행한다")
    @Test
    void attractionEmbeddingMapperRunsTargetAndUpsertQueries() {
        long targetAttractionId = 9300001L;
        long outsideAttractionId = 9300002L;
        String hash = "a".repeat(64);
        seedAttraction(targetAttractionId, "임베딩 대상", 1, 1);
        seedAttraction(outsideAttractionId, "임베딩 외부", 2, 2);

        attractionEmbeddingMapper.upsertFailed(
                outsideAttractionId,
                "v1",
                hash,
                "GMS_ERROR",
                "embedding failed"
        );
        attractionEmbeddingMapper.upsertEmbedded(
                targetAttractionId,
                vectorLiteral(3072),
                "v1",
                hash,
                3072,
                "임베딩 대상",
                "gms",
                "text-embedding-3-large"
        );

        List<TargetRegionRecord> regions = List.of(new TargetRegionRecord(1, 1));
        List<AttractionEmbeddingSourceRecord> targets = attractionEmbeddingMapper.findTargets(regions, 10);

        assertThat(targets)
                .extracting(AttractionEmbeddingSourceRecord::attractionId)
                .contains(targetAttractionId);
        assertThat(attractionEmbeddingMapper.existsEmbeddedWithSameSource(targetAttractionId, "v1", hash))
                .isEqualTo(1);
        assertThat(attractionEmbeddingMapper.countOutsideTargetRegions(regions)).isGreaterThanOrEqualTo(1);
    }

    private static String vectorLiteral(int dimension) {
        return "[" + "0.001,".repeat(dimension - 1) + "0.001]";
    }
}
