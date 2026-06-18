package com.ssafy.enjoytrip.core.api.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.ssafy.enjoytrip.core.api.worker.NotificationWorkerConfiguration;
import com.ssafy.enjoytrip.core.domain.service.NotificationOutboxProcessor;
import com.ssafy.enjoytrip.core.domain.service.NotificationService;
import com.ssafy.enjoytrip.storage.db.core.jpa.FriendshipJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.NotificationJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.NotificationOutboxJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class NotificationWorkerApplicationContextTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestWorkerContextConfiguration.class)
            .withPropertyValues(
                    "spring.main.web-application-type=none",
                    "enjoytrip.notification.outbox.cdc.enabled=false"
            );

    @DisplayName("worker context는 Store 없이 NotificationWorkerConfiguration과 processor를 조립한다")
    @Test
    void workerContextWiresNotificationProcessorWithoutStoreImports() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(NotificationWorkerConfiguration.class);
            assertThat(context).hasSingleBean(NotificationOutboxProcessor.class);
            assertThat(context).hasSingleBean(NotificationService.class);
        });
    }

    @Configuration
    static class TestWorkerContextConfiguration {
        @Bean
        NotificationWorkerConfiguration notificationWorkerConfiguration() {
            return new NotificationWorkerConfiguration();
        }

        @Bean
        NotificationService notificationService(NotificationJpaRepository notificationRepository,
                                                NotificationOutboxJpaRepository outboxRepository,
                                                FriendshipJpaRepository friendshipRepository) {
            return new NotificationService(notificationRepository, outboxRepository, friendshipRepository);
        }

        @Bean
        NotificationOutboxProcessor notificationOutboxProcessor(NotificationService notificationService) {
            return new NotificationOutboxProcessor(notificationService);
        }

        @Bean
        NotificationJpaRepository notificationRepository() {
            return mock(NotificationJpaRepository.class);
        }

        @Bean
        NotificationOutboxJpaRepository outboxRepository() {
            return mock(NotificationOutboxJpaRepository.class);
        }

        @Bean
        FriendshipJpaRepository friendshipRepository() {
            return mock(FriendshipJpaRepository.class);
        }
    }
}
