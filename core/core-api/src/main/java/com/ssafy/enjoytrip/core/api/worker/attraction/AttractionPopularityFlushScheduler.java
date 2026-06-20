package com.ssafy.enjoytrip.core.api.worker.attraction;

import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityDeltaBuffer;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsService;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final AttractionPopularityDeltaBuffer deltaBuffer;
    private final AttractionPopularityStatsService statsService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Value("${enjoytrip.attraction.popularity.flush-batch-size:500}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${enjoytrip.attraction.popularity.flush-delay-ms:1000}")
    public void flushFavoriteDeltas() {
        if (!running.compareAndSet(false, true)) {
            log.debug("Skip attraction popularity flush because the previous run is still active");
            return;
        }

        try {
            Map<Long, Long> deltas = deltaBuffer.drainDirtyDeltas(batchSize);
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
        } finally {
            running.set(false);
        }
    }
}
