package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.ATTRACTION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.AttractionPopularityDeltaCache;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.external.minio.MinioNoteImageUploadUrlGenerator;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberProfileEmbeddingMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteTagMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.TagMapper;
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
                    mock(MemberProfileEmbeddingMapper.class),
                    mock(AttractionPopularityDeltaCache.class),
                    mock(org.springframework.context.ApplicationEventPublisher.class)
            );

            assertThat(service).isNotNull();
        }

        @DisplayName("AttractionService는 장소 저장이 새로 반영된 경우에만 저장 popularity 델타를 기록한다")
        @Test
        void recordsPopularityDeltaWhenSaveInserted() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
            AttractionService service = newAttractionService(attractionMapper, deltaCache);
            when(attractionMapper.insertSave(1L, 11L)).thenReturn(1);

            service.addSave(1L, 11L);

            verify(deltaCache).recordSaveDelta(1L, 1);
        }

        @DisplayName("AttractionService는 중복 장소 저장이면 저장 popularity 델타를 기록하지 않는다")
        @Test
        void skipsPopularityDeltaWhenSaveAlreadyExists() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
            AttractionService service = newAttractionService(attractionMapper, deltaCache);
            when(attractionMapper.insertSave(1L, 11L)).thenReturn(0);

            service.addSave(1L, 11L);

            verify(deltaCache, never()).recordSaveDelta(1L, 1);
        }

        @DisplayName("AttractionService는 장소 저장 해제가 실제 반영된 경우에만 저장 감소 델타를 기록한다")
        @Test
        void recordsPopularityDeltaWhenSaveDeleted() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
            AttractionService service = newAttractionService(attractionMapper, deltaCache);
            when(attractionMapper.deleteSave(1L, 11L)).thenReturn(1);

            boolean deleted = service.removeSave(1L, 11L);

            assertThat(deleted).isTrue();
            verify(deltaCache).recordSaveDelta(1L, -1);
        }

        @DisplayName("AttractionService는 평점 저장 후 rating 집계 row를 갱신한다")
        @Test
        void refreshesPopularityRatingStatsWhenRatingUpserted() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
            AttractionService service = newAttractionService(attractionMapper, deltaCache);

            service.upsertRating(1L, 11L, 5);

            verify(attractionMapper).upsertRating(1L, 11L, 5);
            verify(attractionMapper).refreshPopularityRatingStats(1L);
        }

        @DisplayName("AttractionService는 평점 삭제가 실제 반영된 경우에만 rating 집계 row를 갱신한다")
        @Test
        void refreshesPopularityRatingStatsOnlyWhenRatingDeleted() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
            AttractionService service = newAttractionService(attractionMapper, deltaCache);
            when(attractionMapper.deleteRating(1L, 11L)).thenReturn(1);
            when(attractionMapper.deleteRating(2L, 11L)).thenReturn(0);

            assertThat(service.removeRating(1L, 11L)).isTrue();
            assertThat(service.removeRating(2L, 11L)).isFalse();

            verify(attractionMapper).refreshPopularityRatingStats(1L);
            verify(attractionMapper, never()).refreshPopularityRatingStats(2L);
        }

        @DisplayName("AttractionService는 상세 row를 단건 상세 도메인으로 반환한다")
        @Test
        void findsAttractionDetailFromJoinedRows() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionService service = newAttractionService(
                    attractionMapper,
                    mock(AttractionPopularityDeltaCache.class)
            );
            when(attractionMapper.findDetailById(1L, 11L)).thenReturn(
                    attractionRow(1L)
            );

            Attraction attraction = service.findAttractionDetail(1L, 11L);

            assertThat(attraction.id()).isEqualTo(1L);
            assertThat(attraction.title()).isEqualTo("경복궁");
            assertThat(attraction.saved()).isTrue();
            assertThat(attraction.myRating()).isEqualTo(5);
        }

        @DisplayName("AttractionService는 상세 row가 없으면 관광지 없음 예외를 던진다")
        @Test
        void throwsWhenAttractionDetailRowsAreEmpty() {
            AttractionMapper attractionMapper = mock(AttractionMapper.class);
            AttractionService service = newAttractionService(
                    attractionMapper,
                    mock(AttractionPopularityDeltaCache.class)
            );
            when(attractionMapper.findDetailById(999L, null)).thenReturn(null);

            assertThatThrownBy(() -> service.findAttractionDetail(999L, null))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ATTRACTION_NOT_FOUND);
        }

        private AttractionSearchRecord attractionRow(Long attractionId) {
            return new AttractionSearchRecord(
                    attractionId,
                    "경복궁",
                    "서울 종로구",
                    "",
                    "03045",
                    "02-3700-3900",
                    "image",
                    "image2",
                    7,
                    1,
                    2,
                    37.579617,
                    126.977041,
                    "6",
                    "12",
                    "조선 시대 궁궐입니다.",
                    null,
                    2,
                    4.5,
                    2,
                    true,
                    5
            );
        }

        private AttractionService newAttractionService(AttractionMapper attractionMapper,
                                                       AttractionPopularityDeltaCache deltaCache) {
            return new AttractionService(
                    attractionMapper,
                    mock(MemberProfileEmbeddingMapper.class),
                    deltaCache,
                    mock(org.springframework.context.ApplicationEventPublisher.class)
            );
        }
    }

    @Nested
    class NoteServiceTests {
        @DisplayName("NoteService는 접근 가능한 active 쪽지만 저장한다")
        @Test
        void savesOnlyAccessibleActiveNotes() {
            NoteMapper noteMapper = mock(NoteMapper.class);
            NoteService service = new NoteService(
                    noteMapper,
                    mock(NoteTagMapper.class),
                    mock(TagMapper.class),
                    mock(MemberProfileEmbeddingMapper.class),
                    mock(MinioNoteImageUploadUrlGenerator.class),
                    mock(org.springframework.context.ApplicationEventPublisher.class)
            );
            when(noteMapper.existsAccessibleActive(1L, 11L)).thenReturn(1);

            service.addSave(1L, 11L);

            verify(noteMapper).insertSave(1L, 11L);
        }

        @DisplayName("NoteService는 접근 불가 쪽지를 저장하지 않는다")
        @Test
        void rejectsInaccessibleNoteSave() {
            NoteMapper noteMapper = mock(NoteMapper.class);
            NoteService service = new NoteService(
                    noteMapper,
                    mock(NoteTagMapper.class),
                    mock(TagMapper.class),
                    mock(MemberProfileEmbeddingMapper.class),
                    mock(MinioNoteImageUploadUrlGenerator.class),
                    mock(org.springframework.context.ApplicationEventPublisher.class)
            );
            when(noteMapper.existsAccessibleActive(1L, 11L)).thenReturn(0);

            assertThatThrownBy(() -> service.addSave(1L, 11L))
                    .isInstanceOf(CoreException.class);

            verify(noteMapper, never()).insertSave(1L, 11L);
        }
    }


}
