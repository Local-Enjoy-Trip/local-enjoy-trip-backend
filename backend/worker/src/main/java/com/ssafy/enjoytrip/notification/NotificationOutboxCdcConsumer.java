package com.ssafy.enjoytrip.notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.enjoytrip.service.NotificationOutboxProcessor;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "enjoytrip.notification.outbox.cdc.enabled", havingValue = "true")
public class NotificationOutboxCdcConsumer {
    private static final String PENDING = "PENDING";
    private static final TypeReference<Map<String, Object>> MESSAGE_TYPE = new TypeReference<>() {
    };

    private final NotificationOutboxProcessor outboxProcessor;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${enjoytrip.notification.outbox.cdc.topic}",
            groupId = "${enjoytrip.notification.outbox.cdc.group-id}"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws Exception {
        Map<String, Object> value = value(record);
        if (value == null || shouldIgnore(value)) {
            acknowledge(acknowledgment);
            return;
        }

        Long outboxEventId = numberValue(value.get("id"));
        log.debug("Processing notification outbox CDC event id={} topic={} offset={}",
                outboxEventId,
                record.topic(),
                record.offset());
        outboxProcessor.processOutboxEvent(outboxEventId);
        acknowledge(acknowledgment);
    }

    private Map<String, Object> value(ConsumerRecord<String, String> record) throws Exception {
        if (record.value() == null || record.value().isBlank()) {
            return null;
        }
        return objectMapper.readValue(record.value(), MESSAGE_TYPE);
    }

    private static boolean shouldIgnore(Map<String, Object> value) {
        String op = stringValue(firstPresent(value, "__op", "op"));
        String status = stringValue(value.get("status"));
        return !("c".equals(op) || "r".equals(op)) || !PENDING.equals(status);
    }

    private static Object firstPresent(Map<String, Object> value, String firstKey, String secondKey) {
        Object first = value.get(firstKey);
        return first != null ? first : value.get(secondKey);
    }

    private static Long numberValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        throw new IllegalArgumentException("notification outbox CDC message id is missing");
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static void acknowledge(Acknowledgment acknowledgment) {
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }
}
