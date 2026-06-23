package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.AttractionStats;
import com.ssafy.enjoytrip.core.domain.AttractionStatsReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttractionStatsService {
    private final AttractionStatsReader attractionStatsReader;

    public AttractionStats findStats(Long attractionId, Long memberId) {
        return attractionStatsReader.findStats(attractionId, memberId);
    }

    public List<AttractionStats> findStatsByAttractionIds(List<Long> attractionIds, Long memberId) {
        return attractionStatsReader.findStatsByAttractionIds(attractionIds, memberId);
    }
}
