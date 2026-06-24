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

    @DisplayName("AttractionMapper는 검색, 주변 검색, 태그, 저장, 평점 SQL을 실행한다")
    @Test
    void attractionMapperRunsSearchAndUserInteractionQueries() {
        long attractionId = 9200001L;
        Long memberId = seedMember("attraction-member", uniqueId("attraction") + "@example.com");
        seedAttraction(attractionId, "서비스커넥션 궁", 1, 1);
        jdbcTemplate.update("""
                insert into attraction_popularity_stats (attraction_id, save_count)
                values (?, 7)
                on conflict (attraction_id) do update set save_count = excluded.save_count
                """, attractionId);

        AttractionTagRecord tag = attractionMapper.insertTag(uniqueId("tag"));
        attractionMapper.insertTagMapping(attractionId, tag.id());
        attractionMapper.insertSave(attractionId, memberId);
        attractionMapper.upsertRating(attractionId, memberId, 5);
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
                memberId
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
                attractionMapper.findStatsRowsByAttractionIds(List.of(attractionId), memberId);
        List<AttractionCountRecord> popularityCounts =
                attractionMapper.findPopularityCounts(List.of(attractionId));

        assertThat(attractionMapper.existsById(attractionId)).isEqualTo(1);
        assertThat(searched).extracting(AttractionSearchRecord::id).contains(attractionId);
        assertThat(searched).extracting(AttractionSearchRecord::tagId).contains(tag.id());
        assertThat(searched).extracting(AttractionSearchRecord::saveCount).contains(7);
        assertThat(searched).extracting(AttractionSearchRecord::saved).contains(true);
        assertThat(searched).extracting(AttractionSearchRecord::ratingCount).contains(1);
        assertThat(searched).extracting(AttractionSearchRecord::ratingAverage).contains(5.0);
        assertThat(searched).extracting(AttractionSearchRecord::myRating).contains(5);
        assertThat(nearby).extracting(AttractionSearchRecord::id).contains(attractionId);
        assertThat(attractionMapper.findByIds(List.of(attractionId))).hasSize(1);
        assertThat(attractionMapper.countTagsByIds(List.of(tag.id()))).isEqualTo(1);
        assertThat(attractionMapper.findAllTags()).extracting(AttractionTagRecord::id).contains(tag.id());
        assertThat(statsRows).extracting(AttractionStatsRowRecord::tagId).contains(tag.id());
        assertThat(statsRows).extracting(AttractionStatsRowRecord::saveCount).contains(7);
        assertThat(statsRows).extracting(AttractionStatsRowRecord::saved).contains(true);
        assertThat(statsRows).extracting(AttractionStatsRowRecord::ratingCount).contains(1);
        assertThat(statsRows).extracting(AttractionStatsRowRecord::averageRating).contains(5.0);
        assertThat(statsRows).extracting(AttractionStatsRowRecord::myRating).contains(5);
        assertThat(popularityCounts).extracting(AttractionCountRecord::count).contains(7);
        assertThat(attractionMapper.deleteRating(attractionId, memberId)).isEqualTo(1);
        assertThat(attractionMapper.deleteSave(attractionId, memberId)).isEqualTo(1);
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

    @DisplayName("searchMapPlaces는 관광지 제목으로만 키워드 검색을 수행하며 정확히 일치하는 결과가 먼저 나오도록 랭킹과 거리를 정렬하고 반경 조건을 적용한다")
    @Test
    void searchMapPlacesFiltersAndRanksCorrectly() {
        Long memberId = seedMember("place-viewer", uniqueId("place-viewer") + "@example.com");

        // 1. EXACT match, 가까운 거리 (약 0m)
        long idExactNear = 9200101L;
        seedAttraction(idExactNear, "경복궁", 1, 1);

        // 2. EXACT match, 먼 거리
        long idExactFar = 9200102L;
        jdbcTemplate.update("""
                insert into attractions (id, title, addr1, read_count, sido_code, gugun_code, content_type_id, overview, location)
                values (?, '경복궁', '서울 중구', 10, 1, 1, '12', '경복궁 overview', ST_SetSRID(ST_MakePoint(126.9790, 37.5670), 4326))
                on conflict (id) do nothing
                """, idExactFar);

        // 3. CONTAINS match, 가까운 거리
        long idContainsNear = 9200103L;
        jdbcTemplate.update("""
                insert into attractions (id, title, addr1, read_count, sido_code, gugun_code, content_type_id, overview, location)
                values (?, '아름다운 경복궁', '서울 중구', 10, 1, 1, '12', '경복궁 overview', ST_SetSRID(ST_MakePoint(126.97805, 37.56655), 4326))
                on conflict (id) do nothing
                """, idContainsNear);

        // 4. 주소에만 키워드가 포함된 경우 (검색에서 제외되어야 함)
        long idAddrMatchOnly = 9200104L;
        jdbcTemplate.update("""
                insert into attractions (id, title, addr1, read_count, sido_code, gugun_code, content_type_id, overview, location)
                values (?, '창덕궁', '서울 종로구 경복궁길', 10, 1, 1, '12', '창덕궁 overview', ST_SetSRID(ST_MakePoint(126.9780, 37.5665), 4326))
                on conflict (id) do nothing
                """, idAddrMatchOnly);

        // 5. 개요에만 키워드가 포함된 경우 (검색에서 제외되어야 함)
        long idOverviewMatchOnly = 9200105L;
        jdbcTemplate.update("""
                insert into attractions (id, title, addr1, read_count, sido_code, gugun_code, content_type_id, overview, location)
                values (?, '덕수궁', '서울 중구', 10, 1, 1, '12', '경복궁 근처에 있습니다.', ST_SetSRID(ST_MakePoint(126.9780, 37.5665), 4326))
                on conflict (id) do nothing
                """, idOverviewMatchOnly);

        // 6. 와일드카드 이스케이프 확인용
        long idWildcard = 9200106L;
        jdbcTemplate.update("""
                insert into attractions (id, title, addr1, read_count, sido_code, gugun_code, content_type_id, overview, location)
                values (?, '궁 %_ 특수문자', '서울 중구', 10, 1, 1, '12', 'overview', ST_SetSRID(ST_MakePoint(126.9780, 37.5665), 4326))
                on conflict (id) do nothing
                """, idWildcard);

        // 일반 검색 검증 (radius는 null)
        List<AttractionSearchRecord> results = attractionMapper.searchMapPlaces(
                "경복궁",
                "경복궁",
                126.9780,
                37.5665,
                null,
                50,
                memberId
        );

        // 정렬 순서: EXACT match 중 거리가 가까운 순 -> idExactNear -> idExactFar -> idContainsNear
        // 주소나 개요에만 매칭된 것(idAddrMatchOnly, idOverviewMatchOnly)은 제외
        assertThat(results).extracting(AttractionSearchRecord::id)
                .containsExactly(idExactNear, idExactFar, idContainsNear);

        // 반경 적용(radius=50m) 검색 검증 -> idExactFar (약 100m이상 거리)는 제외되어야 함.
        List<AttractionSearchRecord> radiusResults = attractionMapper.searchMapPlaces(
                "경복궁",
                "경복궁",
                126.9780,
                37.5665,
                50.0, // 50m
                50,
                memberId
        );
        assertThat(radiusResults).extracting(AttractionSearchRecord::id)
                .containsExactly(idExactNear, idContainsNear);

        // 와일드카드 이스케이프 검증
        List<AttractionSearchRecord> wildcardResults = attractionMapper.searchMapPlaces(
                "%",
                "\\%",
                126.9780,
                37.5665,
                null,
                50,
                memberId
        );
        assertThat(wildcardResults).extracting(AttractionSearchRecord::id)
                .containsExactly(idWildcard);
    }
}
