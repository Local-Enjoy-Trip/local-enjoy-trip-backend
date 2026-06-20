package com.ssafy.enjoytrip.core.api.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.ssafy.enjoytrip.core.api.worker.attraction.AttractionPopularityFlushScheduler;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityDeltaCache;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

class WorkerApplicationContextTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestWorkerContextConfiguration.class)
            .withPropertyValues(
                    "spring.main.web-application-type=none",
                    "spring.profiles.active=worker"
            );

    @DisplayName("worker context는 popularity scheduler를 조립한다")
    @Test
    void workerContextWiresPopularityFlushScheduler() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(WorkerConfiguration.class);
            assertThat(context).hasSingleBean(AttractionPopularityDeltaCache.class);
            assertThat(context).hasSingleBean(AttractionPopularityFlushScheduler.class);
        });
    }

    @Configuration
    @Import(WorkerConfiguration.class)
    static class TestWorkerContextConfiguration {
        @Bean
        StringRedisTemplate stringRedisTemplate() {
            return mock(StringRedisTemplate.class);
        }

        @Bean
        AttractionMapper attractionMapper() {
            return mock(AttractionMapper.class);
        }
    }
}
