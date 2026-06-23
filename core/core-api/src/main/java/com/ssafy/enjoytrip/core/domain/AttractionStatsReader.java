package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionStatsRowRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.ArrayList;
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

        return toStats(attractionId, rows);
    }

    public List<AttractionStats> findStatsByAttractionIds(List<Long> attractionIds, Long memberId) {
        if (attractionIds.isEmpty()) {
            return List.of();
        }

        List<AttractionStatsRowRecord> rows = attractionMapper.findStatsRowsByAttractionIds(
                attractionIds,
                memberId
        );

        return toStatsList(rows);
    }

    private AttractionStats emptyStats(Long attractionId) {
        return new AttractionStats(
                attractionId,
                0,
                0.0,
                0,
                List.of(),
                false,
                null
        );
    }

    private List<AttractionStats> toStatsList(List<AttractionStatsRowRecord> rows) {
        List<AttractionStats> stats = new ArrayList<>();
        List<AttractionStatsRowRecord> attractionRows = new ArrayList<>();
        Long currentAttractionId = null;

        for (AttractionStatsRowRecord row : rows) {
            if (currentAttractionId != null && !currentAttractionId.equals(row.attractionId())) {
                stats.add(toStats(currentAttractionId, attractionRows));
                attractionRows.clear();
            }
            currentAttractionId = row.attractionId();
            attractionRows.add(row);
        }

        if (!attractionRows.isEmpty()) {
            stats.add(toStats(currentAttractionId, attractionRows));
        }

        return stats;
    }

    private AttractionStats toStats(Long attractionId, List<AttractionStatsRowRecord> rows) {
        AttractionStatsRowRecord first = rows.get(0);
        return new AttractionStats(
                attractionId,
                first.saveCount(),
                first.averageRating(),
                first.ratingCount(),
                findTags(rows),
                first.saved(),
                first.myRating()
        );
    }

    private List<AttractionTag> findTags(List<AttractionStatsRowRecord> rows) {
        return rows.stream()
                .filter(row -> row.tagId() != null)
                .map(row -> new AttractionTag(row.tagId(), row.tagName()))
                .toList();
    }
}
