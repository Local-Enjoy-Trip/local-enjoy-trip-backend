package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionPopularityDeltaRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttractionPopularityStatsService {
    private final AttractionMapper attractionMapper;

    public int applyFavoriteDeltas(Map<Long, Long> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            return 0;
        }

        List<AttractionPopularityDeltaRecord> records = toDeltaRecords(deltas);
        if (records.isEmpty()) {
            return 0;
        }

        return attractionMapper.applyPopularityFavoriteDeltas(records);
    }

    public int applySaveDeltas(Map<Long, Long> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            return 0;
        }

        List<AttractionPopularityDeltaRecord> records = toDeltaRecords(deltas);
        if (records.isEmpty()) {
            return 0;
        }

        return attractionMapper.applyPopularitySaveDeltas(records);
    }

    private List<AttractionPopularityDeltaRecord> toDeltaRecords(Map<Long, Long> deltas) {
        return deltas.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue() != 0)
                .map(entry -> new AttractionPopularityDeltaRecord(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }
}
