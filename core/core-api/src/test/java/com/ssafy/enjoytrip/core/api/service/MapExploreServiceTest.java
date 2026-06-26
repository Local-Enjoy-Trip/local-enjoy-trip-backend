package com.ssafy.enjoytrip.core.domain.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.external.minio.MinioNoteImageUploadUrlGenerator;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("service")
class MapExploreServiceTest {
    @DisplayName("MapExploreService는 SAVED_PLACE 필터에서 저장 장소 조건을 조회 경로로 전달한다")
    @Test
    void savedPlaceFilterDelegatesSavedOnlyPlaceLookup() {
        AttractionMapper attractionMapper = mock(AttractionMapper.class);
        NoteMapper noteMapper = mock(NoteMapper.class);
        MinioNoteImageUploadUrlGenerator noteImageUploadUrlGenerator = mock(MinioNoteImageUploadUrlGenerator.class);
        MapExploreService service = new MapExploreService(attractionMapper, noteMapper, noteImageUploadUrlGenerator);

        when(attractionMapper.findNearby(
                eq(126.9780),
                eq(37.5665),
                eq(500.0),
                isNull(),
                eq(true),
                eq(11L)
        )).thenReturn(List.of());

        service.explore(
                11L,
                126.9780,
                37.5665,
                500.0,
                MapExploreFilter.SAVED_PLACE,
                null
        );

        verify(attractionMapper).findNearby(
                eq(126.9780),
                eq(37.5665),
                eq(500.0),
                isNull(),
                eq(true),
                eq(11L)
        );
    }
}
