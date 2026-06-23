package com.ssafy.enjoytrip.core.domain;

import java.util.Map;

public interface AttractionPopularityDeltaCache {
    void recordSaveDelta(Long attractionId, long delta);

    Map<Long, Long> drainDirtySaveDeltas(int batchSize);
}
