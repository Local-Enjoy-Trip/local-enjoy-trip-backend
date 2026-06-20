package com.ssafy.enjoytrip.core.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.ssafy.enjoytrip.core.api.worker.attraction.AttractionPopularityFlushScheduler;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsService;
import com.ssafy.enjoytrip.core.domain.service.RedisAttractionPopularityDeltaCache;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

class SchedulingConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestSchedulingContextConfiguration.class);

    @DisplayName("일반 core-api context는 worker profile 없이 popularity flush scheduler를 조립한다")
    @Test
    void coreApiContextWiresPopularityFlushSchedulerWithoutWorkerProfile() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SchedulingConfiguration.class);
            assertThat(context).hasSingleBean(AttractionPopularityFlushScheduler.class);
        });
    }

    @Configuration
    @Import({
            SchedulingConfiguration.class,
            RedisAttractionPopularityDeltaCache.class,
            AttractionPopularityStatsService.class,
            AttractionPopularityFlushScheduler.class
    })
    static class TestSchedulingContextConfiguration {
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
