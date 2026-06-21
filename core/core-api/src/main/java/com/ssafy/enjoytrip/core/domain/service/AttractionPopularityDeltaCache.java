package com.ssafy.enjoytrip.core.domain.service;

import java.util.Map;

public interface AttractionPopularityDeltaCache {
    void recordFavoriteDelta(Long attractionId, long delta);

    void recordSaveDelta(Long attractionId, long delta);

    Map<Long, Long> drainDirtyFavoriteDeltas(int batchSize);

    Map<Long, Long> drainDirtySaveDeltas(int batchSize);
}
