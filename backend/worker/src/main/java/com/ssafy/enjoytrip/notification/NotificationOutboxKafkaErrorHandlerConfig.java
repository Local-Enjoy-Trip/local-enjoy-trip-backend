package com.ssafy.enjoytrip.notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.enjoytrip.service.NotificationOutboxProcessor;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.BackOffExecution;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "enjoytrip.notification.outbox.cdc.enabled", havingValue = "true")
public class NotificationOutboxKafkaErrorHandlerConfig {
    static final long[] RETRY_BACKOFF_MILLIS = {
            60_000L,
            300_000L,
            900_000L,
            3_600_000L,
            10_800_000L
    };
    private static final TypeReference<Map<String, Object>> MESSAGE_TYPE = new TypeReference<>() {
    };

    private final NotificationOutboxProcessor outboxProcessor;
    private final ObjectMapper objectMapper;

    @Bean
    ConsumerFactory<Object, Object> notificationOutboxConsumerFactory(Environment environment) {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getProperty("spring.kafka.bootstrap-servers", "localhost:9092")
        );
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                environment.getProperty(
                        "enjoytrip.notification.outbox.cdc.group-id",
                        "enjoytrip-notification-outbox"
                )
        );
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> notificationOutboxConsumerFactory,
            DefaultErrorHandler notificationOutboxErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationOutboxConsumerFactory);
        factory.setCommonErrorHandler(notificationOutboxErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    DefaultErrorHandler notificationOutboxErrorHandler() {
        return new DefaultErrorHandler(
                this::markFailedWhenRetriesExhausted,
                notificationRetryBackOff()
        );
    }

    static BackOff notificationRetryBackOff() {
        return () -> new BackOffExecution() {
            private int retryIndex;

            @Override
            public long nextBackOff() {
                if (retryIndex >= RETRY_BACKOFF_MILLIS.length) {
                    return BackOffExecution.STOP;
                }
                return RETRY_BACKOFF_MILLIS[retryIndex++];
            }
        };
    }

    private void markFailedWhenRetriesExhausted(ConsumerRecord<?, ?> record, Exception exception) {
        Long outboxEventId = outboxEventId(record);
        if (outboxEventId != null) {
            outboxProcessor.markFailed(outboxEventId, exception.getMessage());
        }
    }

    private Long outboxEventId(ConsumerRecord<?, ?> record) {
        Map<String, Object> map = value(record.value());
        if (map == null) {
            return null;
        }
        Object id = map.get("id");
        if (id instanceof Number number) {
            return number.longValue();
        }
        if (id instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }

    private Map<String, Object> value(Object value) {
        if (!(value instanceof String text) || text.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(text, MESSAGE_TYPE);
        } catch (Exception exception) {
            log.warn("Failed to parse notification outbox CDC record after retry exhaustion", exception);
            return null;
        }
    }
}
