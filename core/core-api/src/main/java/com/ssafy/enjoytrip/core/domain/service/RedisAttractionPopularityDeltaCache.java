package com.ssafy.enjoytrip.core.domain.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisAttractionPopularityDeltaCache implements AttractionPopularityDeltaCache {
    private static final String FAVORITE_DIRTY_IDS_KEY = "enjoytrip:attraction-popularity:favorite:dirty";
    private static final String SAVE_DIRTY_IDS_KEY = "enjoytrip:attraction-popularity:save:dirty";
    private static final String FAVORITE_DELTA_KEY_PREFIX = "enjoytrip:attraction-popularity:favorite:delta:";
    private static final String SAVE_DELTA_KEY_PREFIX = "enjoytrip:attraction-popularity:save:delta:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void recordFavoriteDelta(Long attractionId, long delta) {
        recordDelta(attractionId, delta, FAVORITE_DELTA_KEY_PREFIX, FAVORITE_DIRTY_IDS_KEY, "favorite");
    }

    @Override
    public void recordSaveDelta(Long attractionId, long delta) {
        recordDelta(attractionId, delta, SAVE_DELTA_KEY_PREFIX, SAVE_DIRTY_IDS_KEY, "save");
    }

    @Override
    public Map<Long, Long> drainDirtyFavoriteDeltas(int batchSize) {
        return drainDirtyDeltas(batchSize, FAVORITE_DELTA_KEY_PREFIX, FAVORITE_DIRTY_IDS_KEY, "favorite");
    }

    @Override
    public Map<Long, Long> drainDirtySaveDeltas(int batchSize) {
        return drainDirtyDeltas(batchSize, SAVE_DELTA_KEY_PREFIX, SAVE_DIRTY_IDS_KEY, "save");
    }

    private void recordDelta(
            Long attractionId,
            long delta,
            String deltaKeyPrefix,
            String dirtyIdsKey,
            String kind
    ) {
        if (attractionId == null || delta == 0) {
            return;
        }

        try {
            redisTemplate.opsForValue().increment(deltaKey(deltaKeyPrefix, attractionId), delta);
            redisTemplate.opsForSet().add(dirtyIdsKey, attractionId.toString());
        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to record attraction {} popularity delta. attractionId={}, delta={}",
                    kind,
                    attractionId,
                    delta,
                    exception
            );
        }
    }

    private Map<Long, Long> drainDirtyDeltas(
            int batchSize,
            String deltaKeyPrefix,
            String dirtyIdsKey,
            String kind
    ) {
        if (batchSize <= 0) {
            return Map.of();
        }

        try {
            return drainDirtyDeltas(redisTemplate.opsForSet().pop(dirtyIdsKey, batchSize), deltaKeyPrefix);
        } catch (RuntimeException exception) {
            log.warn("Failed to drain attraction {} popularity deltas", kind, exception);
            return Map.of();
        }
    }

    private Map<Long, Long> drainDirtyDeltas(List<String> dirtyIds, String deltaKeyPrefix) {
        if (dirtyIds == null || dirtyIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> deltas = new LinkedHashMap<>();
        for (String dirtyId : dirtyIds) {
            putDrainedDelta(deltas, dirtyId, deltaKeyPrefix);
        }
        return deltas;
    }

    private void putDrainedDelta(Map<Long, Long> deltas, String dirtyId, String deltaKeyPrefix) {
        Long attractionId = parseAttractionId(dirtyId);
        if (attractionId == null) {
            return;
        }

        Long delta = drainDelta(deltaKeyPrefix, attractionId);
        if (delta == null || delta == 0) {
            return;
        }

        deltas.put(attractionId, delta);
    }

    private Long drainDelta(String deltaKeyPrefix, Long attractionId) {
        String key = deltaKey(deltaKeyPrefix, attractionId);
        String value = redisTemplate.opsForValue().getAndDelete(key);
        if (value == null) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            log.warn("Invalid attraction popularity delta value. key={}, value={}", key, value);
            return null;
        }
    }

    private static Long parseAttractionId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            log.warn("Invalid dirty attraction id. value={}", value);
            return null;
        }
    }

    private static String deltaKey(String prefix, Long attractionId) {
        return prefix + attractionId;
    }
}
