package com.ssafy.enjoytrip.web.sse;

import com.ssafy.enjoytrip.web.dto.response.AiStreamEventResponse;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Authenticated, best-effort live fan-out for AI responses.
 *
 * <p>Redis Stream is used as a low-latency cross-process handoff. This broadcaster intentionally does not
 * promise durable client delivery or disconnected-client replay; callers should treat SSE events as live
 * notifications correlated by authenticated user id and request id.</p>
 */
@Component
public class AiSseEventBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(AiSseEventBroadcaster.class);

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> emittersByClient = new ConcurrentHashMap<>();
    private final ExecutorService deliveryExecutor;

    @Value("${enjoytrip.ai.sse.timeout-millis:180000}")
    private long timeoutMillis;

    public AiSseEventBroadcaster(@Value("${enjoytrip.ai.sse.delivery-threads:2}") int deliveryThreads,
                                 @Value("${enjoytrip.ai.sse.delivery-queue-capacity:100}") int queueCapacity) {
        int threads = Math.max(1, deliveryThreads);
        this.deliveryExecutor = new ThreadPoolExecutor(
                threads,
                threads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(Math.max(1, queueCapacity)),
                new AiSseThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public SseEmitter subscribe(String clientId) {
        String normalizedClientId = normalizeClientId(clientId);
        SseEmitter emitter = new SseEmitter(timeoutMillis);
        emittersByClient.computeIfAbsent(normalizedClientId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(normalizedClientId, emitter));
        emitter.onTimeout(() -> remove(normalizedClientId, emitter));
        emitter.onError(ignored -> remove(normalizedClientId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(new AiStreamEventResponse(null, "connected", normalizedClientId, null, null)));
        } catch (IOException e) {
            remove(normalizedClientId, emitter);
        }
        return emitter;
    }

    public void publish(AiStreamEventResponse event) {
        String targetClientId = normalizeClientId(event.clientId());
        if (targetClientId.isBlank()) {
            log.warn("Dropping AI SSE event id={} action={} because no authenticated target client was provided.",
                    event.id(), event.action());
            return;
        }

        try {
            deliveryExecutor.execute(() -> {
                int delivered = sendToClient(targetClientId, event);
                log.info("Published AI SSE event id={} action={} target={} delivered={}",
                        event.id(), event.action(), targetClientId, delivered);
            });
        } catch (RejectedExecutionException e) {
            log.warn("Dropping AI SSE event id={} action={} because delivery queue is full.", event.id(), event.action());
        }
    }

    @PreDestroy
    void shutdown() {
        deliveryExecutor.shutdownNow();
    }

    private int sendToClient(String clientId, AiStreamEventResponse event) {
        List<SseEmitter> emitters = emittersByClient.get(clientId);
        if (emitters == null || emitters.isEmpty()) {
            return 0;
        }

        int delivered = 0;
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .id(event.id())
                        .name("ai-response")
                        .data(event));
                delivered++;
            } catch (IOException | IllegalStateException e) {
                remove(clientId, emitter);
            }
        }
        return delivered;
    }

    private void remove(String clientId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByClient.get(clientId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByClient.remove(clientId, emitters);
        }
    }

    private static String normalizeClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return "";
        }
        return clientId.trim();
    }

    private static final class AiSseThreadFactory implements ThreadFactory {
        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "ai-sse-delivery-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
