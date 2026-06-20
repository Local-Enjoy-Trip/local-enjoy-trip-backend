package com.ssafy.enjoytrip.core.api.worker.attraction;

import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsService;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("worker")
@RequiredArgsConstructor
public class AttractionPopularityReconcileScheduler {
    private final AttractionPopularityStatsService statsService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(
            initialDelayString = "${enjoytrip.attraction.popularity.reconcile-initial-delay-ms:10000}",
            fixedDelayString = "${enjoytrip.attraction.popularity.reconcile-delay-ms:3600000}"
    )
    public void reconcileFavoriteCounts() {
        if (!running.compareAndSet(false, true)) {
            log.debug("Skip attraction popularity reconcile because the previous run is still active");
            return;
        }

        try {
            int touched = statsService.reconcileFavoriteCounts();
            log.info("Reconciled attraction popularity favorite counts. touched={}", touched);
        } finally {
            running.set(false);
        }
    }
}
