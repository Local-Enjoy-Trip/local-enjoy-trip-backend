package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.MapPin;
import com.ssafy.enjoytrip.core.domain.MapSearchTarget;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.core.domain.PlaceMapPin;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapSearchService {
    private final NoteMapper noteMapper;
    private final AttractionMapper attractionMapper;

    public List<MapPin> search(
            String keyword,
            double longitude,
            double latitude,
            Double radius,
            MapSearchTarget target,
            NoteCategory noteCategory,
            int limit,
            Long viewerMemberId
    ) {
        String escapedKeyword = escapeIlikeWildcards(keyword);

        List<MapPin> merged = new ArrayList<>();

        // 1. 장소(PLACE) 검색
        if (target.includesPlaces()) {
            List<AttractionSearchRecord> records = attractionMapper.searchMapPlaces(
                    keyword,
                    escapedKeyword,
                    longitude,
                    latitude,
                    radius,
                    limit,
                    viewerMemberId
            );
            for (AttractionSearchRecord r : records) {
                int matchTier = r.title().equalsIgnoreCase(keyword) ? 0 : 1;
                merged.add(new PlaceMapPin(
                        r.id(),
                        r.title(),
                        r.addr1(),
                        r.latitude(),
                        r.longitude(),
                        r.firstImage(),
                        r.contentTypeId(),
                        r.distanceMeters(),
                        r.saved(),
                        r.saveCount(),
                        r.ratingAverage(),
                        r.ratingCount(),
                        matchTier
                ));
            }
        }

        // 2. 쪽지(NOTE) 검색
        if (target.includesNotes()) {
            String categoryStr = noteCategory == null ? null : noteCategory.name();
            List<NoteMapPinRecord> records = noteMapper.searchMapNotes(
                    keyword,
                    escapedKeyword,
                    longitude,
                    latitude,
                    radius,
                    categoryStr,
                    limit,
                    viewerMemberId
            );
            for (NoteMapPinRecord r : records) {
                int matchTier = r.title().equalsIgnoreCase(keyword) ? 0 : 1;
                merged.add(new NoteMapPin(
                        r.id(),
                        r.title(),
                        NoteCategory.valueOf(r.category()),
                        NoteVisibility.valueOf(r.visibility()),
                        r.latitude().doubleValue(),
                        r.longitude().doubleValue(),
                        r.regionName(),
                        r.distanceMeters(),
                        r.imageObjectKey(),
                        r.authorNickname(),
                        r.authorProfileImageUrl(),
                        NoteViewerRelationship.valueOf(r.relationship()),
                        r.createdAt(),
                        matchTier
                ));
            }
        }

        // 3. 통합 정렬 (matchTier 오름차순, distanceMeters 오름차순)
        merged.sort(Comparator.comparingInt(MapPin::matchTier)
                .thenComparingDouble(MapPin::distanceMeters));

        return merged;
    }

    private String escapeIlikeWildcards(String keyword) {
        if (keyword == null) {
            return null;
        }
        return keyword.replace("\\", "\\\\")
                      .replace("%", "\\%")
                      .replace("_", "\\_");
    }
}
