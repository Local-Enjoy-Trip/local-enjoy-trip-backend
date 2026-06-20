package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttractionPopularityStatsService {
    private final AttractionMapper attractionMapper;

    public Map<Long, Long> findPopularityFavoriteCounts(List<Long> attractionIds) {
        if (attractionIds == null || attractionIds.isEmpty()) {
            return Map.of();
        }

        return attractionMapper.findPopularityFavoriteCounts(attractionIds).stream()
                .collect(Collectors.toMap(
                        AttractionCountRecord::attractionId,
                        record -> Long.valueOf(record.count())
                ));
    }

    @Transactional
    public int applyFavoriteDeltas(Map<Long, Long> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            return 0;
        }

        int applied = 0;
        for (Map.Entry<Long, Long> entry : deltas.entrySet()) {
            applied += applyFavoriteDelta(entry.getKey(), entry.getValue());
        }
        return applied;
    }

    @Transactional
    public int reconcileFavoriteCounts() {
        int updated = attractionMapper.resetPopularityFavoriteCountsFromFavorites();
        int inserted = attractionMapper.insertMissingPopularityFavoriteCountsFromFavorites();
        return updated + inserted;
    }

    private int applyFavoriteDelta(Long attractionId, Long delta) {
        if (attractionId == null || delta == null || delta == 0) {
            return 0;
        }

        int updated = attractionMapper.updatePopularityFavoriteDelta(attractionId, delta);
        if (updated > 0) {
            return updated;
        }
        return attractionMapper.insertPopularityFavoriteDeltaIfAbsent(attractionId, delta);
    }
}
