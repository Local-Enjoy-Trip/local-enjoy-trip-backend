package com.ssafy.enjoytrip.web.redis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.enjoytrip.web.dto.response.AiStreamEventResponse;
import com.ssafy.enjoytrip.web.sse.AiSseEventBroadcaster;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisStreamAiResponseConsumer implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamAiResponseConsumer.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AiSseEventBroadcaster broadcaster;

    @Value("${enjoytrip.ai.redis.stream-key:enjoytrip-ai-responses}")
    private String streamKey;

    @Value("${enjoytrip.ai.redis.reader-name:${HOSTNAME:enjoytrip-backend}}")
    private String readerName;

    @Value("${enjoytrip.ai.redis.poll.batch-size:10}")
    private long batchSize;

    @Value("${enjoytrip.ai.redis.poll.block-timeout-millis:2000}")
    private long blockTimeoutMillis;

    @Value("${enjoytrip.ai.redis.poll.retry-delay-millis:1000}")
    private long retryDelayMillis;

    private volatile boolean running;
    private Thread worker;
    private String lastSeenId = "$";

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        worker = new Thread(this::readLoop, "ai-redis-stream-reader-" + readerName);
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public synchronized void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void readLoop() {
        log.info("Starting Redis Stream AI reader '{}' on stream '{}'.", readerName, streamKey);
        while (running) {
            try {
                List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                        StreamReadOptions.empty()
                                .count(batchSize)
                                .block(Duration.ofMillis(blockTimeoutMillis)),
                        StreamOffset.create(streamKey, ReadOffset.from(lastSeenId))
                );

                if (records == null || records.isEmpty()) {
                    continue;
                }

                for (MapRecord<String, Object, Object> record : records) {
                    try {
                        handleRecord(record);
                    } catch (Exception e) {
                        log.warn("Skipping invalid AI Redis Stream message id={} on stream '{}': {}",
                                record.getId(), streamKey, e.getMessage());
                    } finally {
                        lastSeenId = record.getId().getValue();
                    }
                }
            } catch (Exception e) {
                if (running) {
                    log.warn("Redis Stream AI reader '{}' failed on stream '{}': {}",
                            readerName, streamKey, e.getMessage());
                    sleepBeforeRetry();
                }
            }
        }
        log.info("Stopped Redis Stream AI reader '{}' on stream '{}'.", readerName, streamKey);
    }

    private void handleRecord(MapRecord<String, Object, Object> record) throws Exception {
        Object payloadValue = record.getValue().get("payload");
        if (!(payloadValue instanceof String payloadJson) || payloadJson.isBlank()) {
            log.warn("Redis Stream AI message {} has no JSON payload field.", record.getId());
            return;
        }

        JsonNode payload = objectMapper.readTree(payloadJson);
        String action = text(payload, "action");
        String clientId = firstText(payload, "clientId", "client_id", "userId", "user_id");
        String requestId = firstText(payload, "requestId", "request_id");

        AiStreamEventResponse event = new AiStreamEventResponse(
                recordId(record.getId()),
                action,
                clientId,
                requestId,
                payload
        );

        log.info("Received AI Redis Stream event id={} action={} target={} requestId={}",
                event.id(), event.action(), event.clientId(), event.requestId());
        broadcaster.publish(event);
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(retryDelayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String recordId(RecordId recordId) {
        return recordId == null ? null : recordId.getValue();
    }

    private static String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = text(node, fieldName);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }
}
