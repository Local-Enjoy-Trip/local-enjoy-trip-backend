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

    @DisplayName("AttractionPopularityFlushScheduler는 저장 델타를 drain해서 RDB 통계에 반영한다")
    @Test
    void flushesCachedSaveDeltasToPopularityStats() {
        AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
        AttractionPopularityStatsService statsService = mock(AttractionPopularityStatsService.class);
        AttractionPopularityFlushScheduler scheduler =
                new AttractionPopularityFlushScheduler(deltaCache, statsService);
        when(deltaCache.drainDirtySaveDeltas(500)).thenReturn(Map.of(3L, 4L));

        scheduler.flushDeltas();

        verify(statsService).applySaveDeltas(Map.of(3L, 4L));
    }

    @DisplayName("AttractionPopularityFlushScheduler는 캐시 델타가 비어 있으면 RDB 통계를 건드리지 않는다")
    @Test
    void skipsStatsUpdateWhenCachedDeltasAreEmpty() {
        AttractionPopularityDeltaCache deltaCache = mock(AttractionPopularityDeltaCache.class);
        AttractionPopularityStatsService statsService = mock(AttractionPopularityStatsService.class);
        AttractionPopularityFlushScheduler scheduler =
                new AttractionPopularityFlushScheduler(deltaCache, statsService);
        when(deltaCache.drainDirtySaveDeltas(500)).thenReturn(Map.of());

        scheduler.flushDeltas();

        verify(statsService, never()).applySaveDeltas(any());
    }
}
