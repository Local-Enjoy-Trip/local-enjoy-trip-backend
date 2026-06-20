package com.ssafy.enjoytrip.core.api.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.ssafy.enjoytrip.core.api.worker.attraction.AttractionPopularityFlushScheduler;
import com.ssafy.enjoytrip.core.api.worker.attraction.AttractionPopularityReconcileScheduler;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityDeltaBuffer;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

class WorkerApplicationContextTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestWorkerContextConfiguration.class)
            .withPropertyValues(
                    "spring.main.web-application-type=none",
                    "spring.profiles.active=worker"
            );

    @DisplayName("worker context는 Kafka outbox 없이 popularity scheduler를 조립한다")
    @Test
    void workerContextWiresPopularitySchedulersWithoutKafkaOutbox() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(WorkerConfiguration.class);
            assertThat(context).hasSingleBean(AttractionPopularityFlushScheduler.class);
            assertThat(context).hasSingleBean(AttractionPopularityReconcileScheduler.class);
        });
    }

    @Configuration
    @ActiveProfiles("worker")
    @Import(WorkerConfiguration.class)
    static class TestWorkerContextConfiguration {
        @Bean
        AttractionPopularityDeltaBuffer attractionPopularityDeltaBuffer() {
            return mock(AttractionPopularityDeltaBuffer.class);
        }

        @Bean
        AttractionPopularityStatsService attractionPopularityStatsService() {
            return mock(AttractionPopularityStatsService.class);
        }
    }
}
