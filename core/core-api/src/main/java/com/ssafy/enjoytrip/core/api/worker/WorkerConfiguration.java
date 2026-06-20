package com.ssafy.enjoytrip.core.api.worker;

import com.ssafy.enjoytrip.core.api.worker.attraction.AttractionPopularityFlushScheduler;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsService;
import com.ssafy.enjoytrip.core.domain.service.RedisAttractionPopularityDeltaCache;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile("worker")
@Import({
        RedisAttractionPopularityDeltaCache.class,
        AttractionPopularityStatsService.class,
        AttractionPopularityFlushScheduler.class
})
public class WorkerConfiguration {
}
