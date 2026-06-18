package com.ssafy.enjoytrip.core.domain.external;

import java.util.Collection;
import java.util.Map;

public interface AttractionPopularityClient {
    Map<Long, Long> findFavoriteCounts(Collection<Long> attractionIds);
}
