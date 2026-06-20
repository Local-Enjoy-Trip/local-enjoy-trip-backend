package com.ssafy.enjoytrip.core.domain.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttractionPopularityDeltaBuffer {
    private static final String FAVORITE_DELTA_KEY = "enjoytrip:attraction-popularity:favorite-deltas";
    private static final RedisScript<List> DRAIN_SCRIPT = RedisScript.of("""
            local entries = redis.call('HGETALL', KEYS[1])
            if next(entries) ~= nil then
                redis.call('DEL', KEYS[1])
            end
            return entries
            """, List.class);

    private final StringRedisTemplate redisTemplate;

    public void recordFavoriteDelta(Long attractionId, int delta) {
        if (attractionId == null || delta == 0) {
            return;
        }

        try {
            redisTemplate.opsForHash().increment(FAVORITE_DELTA_KEY, attractionId.toString(), delta);
        } catch (RuntimeException exception) {
            log.warn(
                    "Attraction popularity delta buffering failed. attractionId={}, delta={}",
                    attractionId,
                    delta,
                    exception
            );
        }
    }

    public Map<Long, Integer> drainFavoriteDeltas() {
        List<?> entries = redisTemplate.execute(DRAIN_SCRIPT, List.of(FAVORITE_DELTA_KEY));
        if (entries == null || entries.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> deltas = new LinkedHashMap<>();
        for (int index = 0; index + 1 < entries.size(); index += 2) {
            addDelta(deltas, entries.get(index), entries.get(index + 1));
        }

        return deltas;
    }

    private void addDelta(Map<Long, Integer> deltas, Object rawAttractionId, Object rawDelta) {
        try {
            Long attractionId = Long.valueOf(rawAttractionId.toString());
            int delta = Integer.parseInt(rawDelta.toString());
            if (delta != 0) {
                deltas.merge(attractionId, delta, Integer::sum);
            }
        } catch (NumberFormatException exception) {
            log.warn(
                    "Invalid attraction popularity delta entry skipped. attractionId={}, delta={}",
                    rawAttractionId,
                    rawDelta,
                    exception
            );
        }
    }
}
