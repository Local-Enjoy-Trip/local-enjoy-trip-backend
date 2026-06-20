package com.ssafy.enjoytrip.core.api.worker;

import com.ssafy.enjoytrip.core.api.config.CoreApiStorageConfiguration;
import com.ssafy.enjoytrip.core.api.worker.attraction.AttractionPopularityFlushScheduler;
import com.ssafy.enjoytrip.core.api.worker.attraction.AttractionPopularityReconcileScheduler;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityDeltaBuffer;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile("worker")
@Import({
        CoreApiStorageConfiguration.class,
        AttractionPopularityDeltaBuffer.class,
        AttractionPopularityStatsService.class,
        AttractionPopularityFlushScheduler.class,
        AttractionPopularityReconcileScheduler.class
})
public class WorkerConfiguration {
}
