package com.ssafy.enjoytrip.core.api.worker.attraction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityDeltaCache;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsService;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttractionPopularityFlushSchedulerTest {

    @DisplayName("AttractionPopularityFlushScheduler는 캐시 델타를 drain해서 RDB 통계에 반영한다")
    @Test
    void flushesCachedFavoriteDeltasToPopularityStats() {
        AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
        AttractionPopularityStatsService statsService = mock(AttractionPopularityStatsService.class);
        AttractionPopularityFlushScheduler scheduler =
                new AttractionPopularityFlushScheduler(deltaCache, statsService);
        when(deltaCache.drainDirtyDeltas(500)).thenReturn(Map.of(1L, 2L, 2L, -1L));

        scheduler.flushFavoriteDeltas();

        verify(statsService).applyFavoriteDeltas(Map.of(1L, 2L, 2L, -1L));
    }

    @DisplayName("AttractionPopularityFlushScheduler는 캐시 델타가 비어 있으면 RDB 통계를 건드리지 않는다")
    @Test
    void skipsStatsUpdateWhenCachedDeltasAreEmpty() {
        AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
        AttractionPopularityStatsService statsService = mock(AttractionPopularityStatsService.class);
        AttractionPopularityFlushScheduler scheduler =
                new AttractionPopularityFlushScheduler(deltaCache, statsService);
        when(deltaCache.drainDirtyDeltas(500)).thenReturn(Map.of());

        scheduler.flushFavoriteDeltas();

        verify(statsService, never()).applyFavoriteDeltas(any());
    }
}
