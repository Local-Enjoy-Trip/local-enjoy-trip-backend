package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.EvChargerMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NewsMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("service")
class DbBackedTravelDataServicesTest {

    @Nested
    class AttractionServiceTests {
        @DisplayName("AttractionService는 MyBatis mapper 기반 의존성으로 생성된다")
        @Test
        void constructsWithMyBatisMapperDependencies() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionService service = new AttractionService(
                    attractionMapper,
                    mock(AttractionPopularityDeltaCache.class)
            );

            assertThat(service).isNotNull();
        }

        @DisplayName("AttractionService는 장소 저장이 새로 반영된 경우에만 저장 popularity 델타를 기록한다")
        @Test
        void recordsPopularityDeltaWhenSaveInserted() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
            AttractionService service = newAttractionService(attractionMapper, deltaCache);
            when(attractionMapper.insertSave(1L, "ssafy")).thenReturn(1);

            service.addSave(1L, "ssafy");

            verify(deltaCache).recordSaveDelta(1L, 1);
        }

        @DisplayName("AttractionService는 중복 장소 저장이면 저장 popularity 델타를 기록하지 않는다")
        @Test
        void skipsPopularityDeltaWhenSaveAlreadyExists() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
            AttractionService service = newAttractionService(attractionMapper, deltaCache);
            when(attractionMapper.insertSave(1L, "ssafy")).thenReturn(0);

            service.addSave(1L, "ssafy");

            verify(deltaCache, never()).recordSaveDelta(1L, 1);
        }

        @DisplayName("AttractionService는 장소 저장 해제가 실제 반영된 경우에만 저장 감소 델타를 기록한다")
        @Test
        void recordsPopularityDeltaWhenSaveDeleted() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
            AttractionService service = newAttractionService(attractionMapper, deltaCache);
            when(attractionMapper.deleteSave(1L, "ssafy")).thenReturn(1);

            boolean deleted = service.removeSave(1L, "ssafy");

            assertThat(deleted).isTrue();
            verify(deltaCache).recordSaveDelta(1L, -1);
        }

        @DisplayName("AttractionService는 평점 저장 후 rating 집계 row를 갱신한다")
        @Test
        void refreshesPopularityRatingStatsWhenRatingUpserted() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
            AttractionService service = newAttractionService(attractionMapper, deltaCache);

            service.upsertRating(1L, "ssafy", 5);

            verify(attractionMapper).upsertRating(1L, "ssafy", 5);
            verify(attractionMapper).refreshPopularityRatingStats(1L);
        }

        @DisplayName("AttractionService는 평점 삭제가 실제 반영된 경우에만 rating 집계 row를 갱신한다")
        @Test
        void refreshesPopularityRatingStatsOnlyWhenRatingDeleted() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
            AttractionService service = newAttractionService(attractionMapper, deltaCache);
            when(attractionMapper.deleteRating(1L, "ssafy")).thenReturn(1);
            when(attractionMapper.deleteRating(2L, "ssafy")).thenReturn(0);

            assertThat(service.removeRating(1L, "ssafy")).isTrue();
            assertThat(service.removeRating(2L, "ssafy")).isFalse();

            verify(attractionMapper).refreshPopularityRatingStats(1L);
            verify(attractionMapper, never()).refreshPopularityRatingStats(2L);
        }

        private AttractionService newAttractionService(AttractionMapper attractionMapper,
                                                       AttractionPopularityDeltaCache deltaCache) {
            return new AttractionService(
                    attractionMapper,
                    deltaCache
            );
        }
    }

    @Nested
    class NoteServiceTests {
        @DisplayName("NoteService는 접근 가능한 active 쪽지만 저장한다")
        @Test
        void savesOnlyAccessibleActiveNotes() {
            NoteMapper noteMapper = mock(NoteMapper.class);
            NoteService service = new NoteService(noteMapper);
            when(noteMapper.existsAccessibleActive(1L, "ssafy")).thenReturn(1);

            service.addSave(1L, "ssafy");

            verify(noteMapper).insertSave(1L, "ssafy");
        }

        @DisplayName("NoteService는 접근 불가 쪽지를 저장하지 않는다")
        @Test
        void rejectsInaccessibleNoteSave() {
            NoteMapper noteMapper = mock(NoteMapper.class);
            NoteService service = new NoteService(noteMapper);
            when(noteMapper.existsAccessibleActive(1L, "ssafy")).thenReturn(0);

            assertThatThrownBy(() -> service.addSave(1L, "ssafy"))
                    .isInstanceOf(CoreException.class);

            verify(noteMapper, never()).insertSave(1L, "ssafy");
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
