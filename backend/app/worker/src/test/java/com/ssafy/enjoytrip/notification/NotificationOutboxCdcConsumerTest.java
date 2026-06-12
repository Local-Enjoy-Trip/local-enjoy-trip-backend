package com.ssafy.enjoytrip.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.enjoytrip.service.NotificationOutboxProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.util.backoff.BackOffExecution;

class NotificationOutboxCdcConsumerTest {

    @DisplayName("대기 create CDC 메시지는 outbox processor를 호출하고 ack 처리한다")
    @Test
    void pendingCreateMessageProcessesOutboxAndAcknowledges() throws Exception {
        NotificationOutboxProcessor processor = mock(NotificationOutboxProcessor.class);
        NotificationOutboxCdcConsumer consumer = new NotificationOutboxCdcConsumer(processor, new ObjectMapper());
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        consumer.consume(record("{\"id\":10,\"status\":\"PENDING\",\"__op\":\"c\"}"), acknowledgment);

        verify(processor).processOutboxEvent(10L);
        verify(acknowledgment).acknowledge();
    }

    @DisplayName("processed update CDC 메시지는 재처리 loop를 막기 위해 processor를 호출하지 않고 ack 처리한다")
    @Test
    void processedUpdateMessageIsIgnoredAndAcknowledged() throws Exception {
        NotificationOutboxProcessor processor = mock(NotificationOutboxProcessor.class);
        NotificationOutboxCdcConsumer consumer = new NotificationOutboxCdcConsumer(processor, new ObjectMapper());
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        consumer.consume(record("{\"id\":10,\"status\":\"PROCESSED\",\"__op\":\"u\"}"), acknowledgment);

        verify(processor, never()).processOutboxEvent(10L);
        verify(acknowledgment).acknowledge();
    }

    @DisplayName("알림 outbox Kafka retry backoff는 1분 5분 15분 60분 180분 뒤 종료된다")
    @Test
    void retryBackOffUsesFixedPolicySequence() {
        BackOffExecution execution = NotificationOutboxKafkaErrorHandlerConfig
                .notificationRetryBackOff()
                .start();

        assertEquals(60_000L, execution.nextBackOff());
        assertEquals(300_000L, execution.nextBackOff());
        assertEquals(900_000L, execution.nextBackOff());
        assertEquals(3_600_000L, execution.nextBackOff());
        assertEquals(10_800_000L, execution.nextBackOff());
        assertEquals(BackOffExecution.STOP, execution.nextBackOff());
    }

    private static ConsumerRecord<String, String> record(String value) {
        return new ConsumerRecord<>("enjoytrip.public.notification_outbox", 0, 0L, null, value);
    }
}
