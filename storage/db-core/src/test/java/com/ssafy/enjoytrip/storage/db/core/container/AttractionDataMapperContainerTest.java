package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionAverageRatingRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionEmbeddingSourceRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRatingRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionTagRecord;
import com.ssafy.enjoytrip.storage.db.core.model.ChargerItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseBriefingCandidateRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NewsItemRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionEmbeddingMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionEmbeddingMapper.TargetRegionRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.EvChargerMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NeighborhoodBriefingMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NewsMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AttractionDataMapperContainerTest extends StorageContainerTestSupport {
    @Autowired
    private AttractionMapper attractionMapper;

    @Autowired
    private AttractionEmbeddingMapper attractionEmbeddingMapper;

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private EvChargerMapper evChargerMapper;

    @Autowired
    private NeighborhoodBriefingMapper neighborhoodBriefingMapper;

    @DisplayName("AttractionMapper는 검색, 주변 검색, 태그, 좋아요, 평점 SQL을 실행한다")
    @Test
    void attractionMapperRunsSearchAndUserInteractionQueries() {
        long attractionId = 9200001L;
        String userId = uniqueId("attraction-user");
        seedAttraction(attractionId, "서비스커넥션 궁", 1, 1);

        AttractionTagRecord tag = attractionMapper.insertTag(uniqueId("tag"));
        attractionMapper.insertTagMapping(attractionId, tag.id());
        attractionMapper.insertFavorite(attractionId, userId);
        attractionMapper.upsertRating(attractionId, userId, 5);

        List<AttractionSearchRecord> searched = attractionMapper.search(
                "12",
                "서비스커넥션",
                1,
                1,
                null,
                null,
                null,
                false,
                10
        );
        List<AttractionSearchRecord> nearby = attractionMapper.findNearby(126.9781, 37.5666, 100, 10);
        List<AttractionCountRecord> favoriteCounts =
                attractionMapper.findFavoriteCounts(List.of(attractionId));
        List<AttractionAverageRatingRecord> ratings = attractionMapper.findRatingStats(List.of(attractionId));
        List<AttractionRatingRecord> myRatings =
                attractionMapper.findMyRatings(List.of(attractionId), userId);

        assertThat(attractionMapper.existsById(attractionId)).isEqualTo(1);
        assertThat(searched).extracting(AttractionSearchRecord::id).contains(attractionId);
        assertThat(nearby).extracting(AttractionSearchRecord::id).contains(attractionId);
        assertThat(attractionMapper.findByIds(List.of(attractionId))).hasSize(1);
        assertThat(attractionMapper.countTagsByIds(List.of(tag.id()))).isEqualTo(1);
        assertThat(attractionMapper.findAllTags()).extracting(AttractionTagRecord::id).contains(tag.id());
        assertThat(attractionMapper.findTagsByAttractionId(attractionId)).extracting(AttractionTagRecord::id)
                .contains(tag.id());
        assertThat(attractionMapper.findFavoritedIds(List.of(attractionId), userId)).contains(attractionId);
        assertThat(favoriteCounts).extracting(AttractionCountRecord::count).contains(1);
        assertThat(ratings).extracting(AttractionAverageRatingRecord::count).contains(1);
        assertThat(myRatings).extracting(AttractionRatingRecord::rating).contains(5);
        assertThat(attractionMapper.deleteRating(attractionId, userId)).isEqualTo(1);
        assertThat(attractionMapper.deleteFavorite(attractionId, userId)).isEqualTo(1);
        assertThat(attractionMapper.deleteTagMappings(attractionId)).isEqualTo(1);
        assertThat(attractionMapper.deleteTag(tag.id())).isEqualTo(1);
    }

    @DisplayName("NewsMapper와 EvChargerMapper는 seed된 운영 테이블을 조건 검색한다")
    @Test
    void newsAndEvChargerMappersReadSeededRows() {
        String newsId = uniqueId("news");
        jdbcTemplate.update("""
                insert into news_items (id, title, link, summary, source, published_at)
                values (?, '서비스커넥션 뉴스', 'https://example.com/news', '요약', 'test', '2026-06-19')
                """, newsId);
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
                values (?, '서비스커넥션 충전소', '01', 'FAST', '서울 중구', '시청 앞', '24H', '사업자', '02',
                        'AVAILABLE', ST_SetSRID(ST_MakePoint(126.9780, 37.5665), 4326))
                """, uniqueId("charger"));

        List<NewsItemRecord> news = newsMapper.findLatest(10);
        List<ChargerItemRecord> chargers = evChargerMapper.findChargers("서울", "서비스커넥션", 10, 0);

        assertThat(news).extracting(NewsItemRecord::id).contains(newsId);
        assertThat(chargers).extracting(ChargerItemRecord::statNm).contains("서비스커넥션 충전소");
    }

    @DisplayName("NeighborhoodBriefingMapper는 공개 READY 코스 후보를 지역 우선순위로 조회한다")
    @Test
    void neighborhoodBriefingMapperFindsPublicReadyCourses() {
        String ownerId = uniqueId("course-owner");
        String courseId = uniqueId("course");
        seedMember(ownerId, ownerId + "@example.com");
        jdbcTemplate.update("""
                insert into courses (id, owner_user_id, title, region_name, visibility, status)
                values (?, ?, '서비스커넥션 코스', '서울 중구', 'PUBLIC', 'READY')
                """, courseId, ownerId);

        List<CourseBriefingCandidateRecord> candidates =
                neighborhoodBriefingMapper.findPublicReadyCandidates("서울 중구", 10);

        assertThat(candidates).extracting(CourseBriefingCandidateRecord::id).contains(courseId);
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
