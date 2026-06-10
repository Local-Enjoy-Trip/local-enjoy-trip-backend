package com.ssafy.enjoytrip.repository;

import java.util.Collection;
import java.util.Map;

public interface AttractionPopularityRepository {
    Map<Long, Long> findFavoriteCounts(Collection<Long> attractionIds);
}
