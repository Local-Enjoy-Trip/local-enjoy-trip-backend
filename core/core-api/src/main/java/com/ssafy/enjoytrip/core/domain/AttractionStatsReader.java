package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionStatsRowRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttractionStatsReader {
    private final AttractionMapper attractionMapper;

    public AttractionStats findStats(Long attractionId, Long memberId) {
        List<AttractionStatsRowRecord> rows = attractionMapper.findStatsRowsByAttractionId(
                attractionId,
                memberId
        );
        if (rows.isEmpty()) {
            return emptyStats(attractionId);
        }

        return toStats(attractionId, rows.get(0));
    }

    public List<AttractionStats> findStatsByAttractionIds(List<Long> attractionIds, Long memberId) {
        if (attractionIds.isEmpty()) {
            return List.of();
        }

        List<AttractionStatsRowRecord> rows = attractionMapper.findStatsRowsByAttractionIds(
                attractionIds,
                memberId
        );

        return rows.stream()
                .map(row -> toStats(row.attractionId(), row))
                .toList();
    }

    private AttractionStats emptyStats(Long attractionId) {
        return new AttractionStats(
                attractionId,
                0,
                0.0,
                0,
                false,
                null
        );
    }

    private AttractionStats toStats(Long attractionId, AttractionStatsRowRecord row) {
        return new AttractionStats(
                attractionId,
                row.saveCount(),
                row.averageRating(),
                row.ratingCount(),
                row.saved(),
                row.myRating()
        );
    }
}
