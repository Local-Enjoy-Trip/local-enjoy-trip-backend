package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.EvChargerMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NewsMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("service")
class DbBackedTravelDataServicesTest {

    @Nested
    class AttractionServiceTests {
        @DisplayName("AttractionService는 MyBatis mapper 기반 통계 의존성으로 생성된다")
        @Test
        void constructsWithMyBatisMapperDependencies() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionService service = new AttractionService(
                    attractionMapper,
                    new AttractionStatsService(attractionMapper),
                    new AttractionPopularityStatsService(attractionMapper),
                    mock(AttractionPopularityDeltaBuffer.class)
            );

            assertThat(service).isNotNull();
        }

        @DisplayName("인기 주변 관광지는 RDB 인기 통계의 favorite_count 기준으로 정렬한다")
        @Test
        void popularNearbyAttractionsUseRdbPopularityStatsForOrdering() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionStatsService statsService = mock(AttractionStatsService.class);
            AttractionService service = new AttractionService(attractionMapper, statsService);
            NearbySearchCondition condition = new NearbySearchCondition(126.9780, 37.5665, 500.0, 20);

            when(attractionMapper.findNearby(126.9780, 37.5665, 500.0, 20))
                    .thenReturn(List.of(
                            attractionSearchRecord(1L, "가까운 0점", 10.0),
                            attractionSearchRecord(2L, "먼 5점", 100.0),
                            attractionSearchRecord(3L, "가까운 5점", 50.0)
                    ));
            when(statsService.findStatsByAttractionId(List.of(1L, 2L, 3L), null))
                    .thenReturn(Map.of());
            when(attractionMapper.findPopularityFavoriteCounts(List.of(1L, 2L, 3L)))
                    .thenReturn(List.of(
                            new AttractionCountRecord(2L, 5),
                            new AttractionCountRecord(3L, 5)
                    ));

            List<PopularAttraction> attractions = service.findPopularNearbyAttractions(condition, null);

            assertThat(attractions)
                    .extracting(attraction -> attraction.attraction().id())
                    .containsExactly(3L, 2L, 1L);
            assertThat(attractions)
                    .extracting(PopularAttraction::popularityCount)
                    .containsExactly(5L, 5L, 0L);
        }

        private AttractionSearchRecord attractionSearchRecord(Long id, String title, Double distanceMeters) {
            return new AttractionSearchRecord(
                    id,
                    title,
                    "서울 중구",
                    "",
                    "zip",
                    "tel",
                    "image",
                    "image2",
                    10,
                    1,
                    1,
                    37.5665,
                    126.9780,
                    "6",
                    "12",
                    "overview",
                    distanceMeters
            );
        }
    }

    @Nested
    class EvChargerServiceTests {
        @DisplayName("EvChargerService는 MyBatis mapper로 생성된다")
        @Test
        void constructsWithMyBatisMapper() {
            assertThat(new EvChargerService(mock(EvChargerMapper.class))).isNotNull();
        }
    }

    @Nested
    class NewsServiceTests {
        @DisplayName("NewsService는 MyBatis mapper로 생성된다")
        @Test
        void constructsWithMyBatisMapper() {
            assertThat(new NewsService(mock(NewsMapper.class))).isNotNull();
        }
    }
}
