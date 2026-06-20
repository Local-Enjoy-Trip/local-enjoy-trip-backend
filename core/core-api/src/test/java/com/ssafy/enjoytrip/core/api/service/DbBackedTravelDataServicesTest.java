package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityDeltaBuffer;
import com.ssafy.enjoytrip.external.ClickHouseAttractionPopularityClient;
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
                    mock(AttractionPopularityDeltaBuffer.class)
            );

            assertThat(service).isNotNull();
        }

        @DisplayName("AttractionService는 찜이 새로 저장된 경우에만 popularity 델타를 기록한다")
        @Test
        void recordsPopularityDeltaWhenFavoriteInserted() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaBuffer deltaBuffer = mock(AttractionPopularityDeltaBuffer.class);
            AttractionService service = newAttractionService(attractionMapper, deltaBuffer);
            when(attractionMapper.insertFavorite(1L, "ssafy")).thenReturn(1);

            service.addFavorite(1L, "ssafy");

            verify(deltaBuffer).recordFavoriteDelta(1L, 1);
        }

        @DisplayName("AttractionService는 중복 찜 저장이면 popularity 델타를 기록하지 않는다")
        @Test
        void skipsPopularityDeltaWhenFavoriteAlreadyExists() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaBuffer deltaBuffer = mock(AttractionPopularityDeltaBuffer.class);
            AttractionService service = newAttractionService(attractionMapper, deltaBuffer);
            when(attractionMapper.insertFavorite(1L, "ssafy")).thenReturn(0);

            service.addFavorite(1L, "ssafy");

            verify(deltaBuffer, never()).recordFavoriteDelta(1L, 1);
        }

        @DisplayName("AttractionService는 찜 삭제가 실제 반영된 경우에만 popularity 감소 델타를 기록한다")
        @Test
        void recordsPopularityDeltaWhenFavoriteDeleted() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaBuffer deltaBuffer = mock(AttractionPopularityDeltaBuffer.class);
            AttractionService service = newAttractionService(attractionMapper, deltaBuffer);
            when(attractionMapper.deleteFavorite(1L, "ssafy")).thenReturn(1);

            boolean deleted = service.removeFavorite(1L, "ssafy");

            assertThat(deleted).isTrue();
            verify(deltaBuffer).recordFavoriteDelta(1L, -1);
        }

        @DisplayName("AttractionService는 삭제할 찜이 없으면 popularity 감소 델타를 기록하지 않는다")
        @Test
        void skipsPopularityDeltaWhenFavoriteDoesNotExist() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaBuffer deltaBuffer = mock(AttractionPopularityDeltaBuffer.class);
            AttractionService service = newAttractionService(attractionMapper, deltaBuffer);
            when(attractionMapper.deleteFavorite(1L, "ssafy")).thenReturn(0);

            boolean deleted = service.removeFavorite(1L, "ssafy");

            assertThat(deleted).isFalse();
            verify(deltaBuffer, never()).recordFavoriteDelta(1L, -1);
        }

        private AttractionService newAttractionService(AttractionMapper attractionMapper,
                                                       AttractionPopularityDeltaBuffer deltaBuffer) {
            return new AttractionService(
                    mock(ClickHouseAttractionPopularityClient.class),
                    attractionMapper,
                    new AttractionStatsService(attractionMapper),
                    deltaBuffer
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
