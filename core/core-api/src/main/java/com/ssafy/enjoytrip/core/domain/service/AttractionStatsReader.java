package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.AttractionStats;
import com.ssafy.enjoytrip.core.domain.AttractionTag;
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

    public AttractionStats findStats(Long attractionId, String userId) {
        List<AttractionStatsRowRecord> rows = attractionMapper.findStatsRowsByAttractionId(
                attractionId,
                userId
        );
        if (rows.isEmpty()) {
            return emptyStats(attractionId);
        }

        return toStats(attractionId, rows);
    }

    public List<AttractionStats> findStatsByAttractionIds(List<Long> attractionIds, String userId) {
        if (attractionIds.isEmpty()) {
            return List.of();
        }

        List<AttractionStatsRowRecord> rows = attractionMapper.findStatsRowsByAttractionIds(
                attractionIds,
                userId
        );

        return toStatsList(rows);
    }

    private AttractionStats emptyStats(Long attractionId) {
        return new AttractionStats(
                attractionId,
                0,
                0,
                0.0,
                0,
                List.of(),
                false,
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
                first.favoriteCount(),
                first.saveCount(),
                first.averageRating(),
                first.ratingCount(),
                findTags(rows),
                first.favorited(),
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
