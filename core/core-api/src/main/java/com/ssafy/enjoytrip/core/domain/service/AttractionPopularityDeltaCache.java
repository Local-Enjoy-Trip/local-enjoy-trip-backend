package com.ssafy.enjoytrip.core.domain.service;

import java.util.Map;

public interface AttractionPopularityDeltaCache {
    void recordFavoriteDelta(Long attractionId, long delta);

    Map<Long, Long> drainDirtyDeltas(int batchSize);
}
