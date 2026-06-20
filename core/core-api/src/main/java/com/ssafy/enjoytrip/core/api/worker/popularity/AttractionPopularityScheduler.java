package com.ssafy.enjoytrip.core.api.worker.popularity;

import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsMaintenanceService;
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
public class AttractionPopularityScheduler {
    private final AttractionPopularityStatsMaintenanceService maintenanceService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(
            fixedDelayString = "${enjoytrip.popularity.favorite.flush-delay-ms:10000}",
            initialDelayString = "${enjoytrip.popularity.favorite.flush-initial-delay-ms:5000}"
    )
    void flushFavoriteDeltas() {
        runSingleFlight("flush", () -> {
            int appliedCount = maintenanceService.flushBufferedFavoriteDeltas();
            if (appliedCount > 0) {
                log.info("Flushed attraction popularity favorite deltas. appliedCount={}", appliedCount);
            }
        });
    }

    @Scheduled(
            fixedDelayString = "${enjoytrip.popularity.favorite.reconcile-delay-ms:60000}",
            initialDelayString = "${enjoytrip.popularity.favorite.reconcile-initial-delay-ms:30000}"
    )
    void reconcileFavoriteCounts() {
        runSingleFlight("reconcile", () -> {
            int reconciledCount = maintenanceService.reconcileFavoriteCounts();
            log.info("Reconciled attraction popularity favorite counts. affectedCount={}", reconciledCount);
        });
    }

    private void runSingleFlight(String operation, Runnable task) {
        if (!running.compareAndSet(false, true)) {
            log.debug("Attraction popularity {} skipped; another maintenance run is active", operation);
            return;
        }

        try {
            task.run();
        } catch (RuntimeException exception) {
            log.warn("Attraction popularity {} failed", operation, exception);
        } finally {
            running.set(false);
        }
    }
}
