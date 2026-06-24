package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.api.web.dto.request.MapSearchRequest;
import com.ssafy.enjoytrip.core.domain.MapPin;
import com.ssafy.enjoytrip.core.domain.MapSearchTarget;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MapSearchServiceTest {

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private AttractionMapper attractionMapper;

    @InjectMocks
    private MapSearchService mapSearchService;

    @DisplayName("MapSearchService는 target이 ALL인 경우 장소와 쪽지를 모두 검색한 후 정렬하여 반환한다")
    @Test
    void searchAllTargetsAndMergeAndSort() {
        // given
        MapSearchRequest request = new MapSearchRequest("경복궁", 126.9780, 37.5665, 500.0, MapSearchTarget.ALL, NoteCategory.TIP, 10);
        Long viewerMemberId = 1L;

        AttractionSearchRecord attractionRecord = new AttractionSearchRecord(
                101L, "아름다운 경복궁", "서울", "중구", "123", "02",
                "image.png", null, 10, 1, 1, 37.5665, 126.9780,
                "1", "12", "overview", 100.0, 1, 4.5, 3,
                false, null
        );

        NoteMapPinRecord noteRecord = new NoteMapPinRecord(
                1L, "서울 산책 메모", "TIP", "PUBLIC",
                BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780), "서울 중구",
                null, 10L, "동네핀러", null, "NONE", LocalDateTime.now(), 42.0
        );

        when(attractionMapper.searchMapPlaces(eq("경복궁"), eq("경복궁"), eq(126.9780), eq(37.5665), eq(500.0), eq(10), eq(viewerMemberId)))
                .thenReturn(List.of(attractionRecord));

        when(noteMapper.searchMapNotes(eq("경복궁"), eq("경복궁"), eq(126.9780), eq(37.5665), eq(500.0), eq("TIP"), eq(10), eq(viewerMemberId)))
                .thenReturn(List.of(noteRecord));

        // when
        List<MapPin> results = mapSearchService.search(
                request.requiredKeyword(),
                request.requiredLongitude(),
                request.requiredLatitude(),
                request.radius(),
                request.normalizedTarget(),
                request.noteCategory(),
                request.cappedLimit(),
                viewerMemberId
        );

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).type()).isEqualTo("NOTE");
        assertThat(results.get(0).distanceMeters()).isEqualTo(42.0);
        assertThat(results.get(1).type()).isEqualTo("PLACE");
        assertThat(results.get(1).distanceMeters()).isEqualTo(100.0);
    }

    @DisplayName("MapSearchService는 target이 PLACE인 경우 쪽지 검색을 생략한다")
    @Test
    void searchPlaceOnly() {
        // given
        MapSearchRequest request = new MapSearchRequest("경복궁", 126.9780, 37.5665, 500.0, MapSearchTarget.PLACE, null, 10);
        Long viewerMemberId = 1L;

        when(attractionMapper.searchMapPlaces(any(), any(), any(Double.class), any(Double.class), any(), any(), any()))
                .thenReturn(List.of());

        // when
        mapSearchService.search(
                request.requiredKeyword(),
                request.requiredLongitude(),
                request.requiredLatitude(),
                request.radius(),
                request.normalizedTarget(),
                request.noteCategory(),
                request.cappedLimit(),
                viewerMemberId
        );

        // then
        verify(attractionMapper).searchMapPlaces(eq("경복궁"), eq("경복궁"), eq(126.9780), eq(37.5665), eq(500.0), eq(10), eq(viewerMemberId));
        verify(noteMapper, never()).searchMapNotes(any(), any(), any(Double.class), any(Double.class), any(), any(), any(), any());
    }

    @DisplayName("MapSearchService는 target이 NOTE인 경우 장소 검색을 생략한다")
    @Test
    void searchNoteOnly() {
        // given
        MapSearchRequest request = new MapSearchRequest("경복궁", 126.9780, 37.5665, 500.0, MapSearchTarget.NOTE, null, 10);
        Long viewerMemberId = 1L;

        when(noteMapper.searchMapNotes(any(), any(), any(Double.class), any(Double.class), any(), any(), any(), any()))
                .thenReturn(List.of());

        // when
        mapSearchService.search(
                request.requiredKeyword(),
                request.requiredLongitude(),
                request.requiredLatitude(),
                request.radius(),
                request.normalizedTarget(),
                request.noteCategory(),
                request.cappedLimit(),
                viewerMemberId
        );

        // then
        verify(noteMapper).searchMapNotes(eq("경복궁"), eq("경복궁"), eq(126.9780), eq(37.5665), eq(500.0), eq(null), eq(10), eq(viewerMemberId));
        verify(attractionMapper, never()).searchMapPlaces(any(), any(), any(Double.class), any(Double.class), any(), any(), any());
    }

    @DisplayName("MapSearchService는 와일드카드 문자가 있는 키워드를 이스케이프하여 전달한다")
    @Test
    void searchWithWildcardCharacters() {
        // given
        MapSearchRequest request = new MapSearchRequest("100%_\\물", 126.9780, 37.5665, null, MapSearchTarget.PLACE, null, null);
        Long viewerMemberId = 1L;

        when(attractionMapper.searchMapPlaces(any(), any(), any(Double.class), any(Double.class), any(), any(), any()))
                .thenReturn(List.of());

        // when
        mapSearchService.search(
                request.requiredKeyword(),
                request.requiredLongitude(),
                request.requiredLatitude(),
                request.radius(),
                request.normalizedTarget(),
                request.noteCategory(),
                request.cappedLimit(),
                viewerMemberId
        );

        // then
        verify(attractionMapper).searchMapPlaces(
                eq("100%_\\물"),
                eq("100\\%\\_\\\\물"),
                eq(126.9780),
                eq(37.5665),
                eq(null),
                eq(50),
                eq(viewerMemberId)
        );
    }
}
