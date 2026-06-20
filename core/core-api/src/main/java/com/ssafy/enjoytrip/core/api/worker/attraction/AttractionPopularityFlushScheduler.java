package com.ssafy.enjoytrip.core.api.worker.attraction;

import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityDeltaCache;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("worker")
@RequiredArgsConstructor
public class AttractionPopularityFlushScheduler {
    private final AttractionPopularityDeltaCache deltaCache;
    private final AttractionPopularityStatsService statsService;

    @Value("${enjoytrip.attraction.popularity.flush-batch-size:500}")
    private int batchSize = 500;

    @Scheduled(fixedDelayString = "${enjoytrip.attraction.popularity.flush-delay-ms:1000}")
    public void flushFavoriteDeltas() {
        Map<Long, Long> deltas = deltaCache.drainDirtyDeltas(batchSize);
        if (deltas.isEmpty()) {
            return;
        }
        int applied = statsService.applyFavoriteDeltas(deltas);
        long deltaSum = deltas.values().stream().mapToLong(Long::longValue).sum();
        log.info(
                "Flushed attraction popularity deltas. dirtyCount={}, applied={}, deltaSum={}",
                deltas.size(),
                applied,
                deltaSum
        );
    }
}
