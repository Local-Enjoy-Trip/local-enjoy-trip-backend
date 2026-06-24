package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.MapPin;
import com.ssafy.enjoytrip.core.domain.MapSearchTarget;
import com.ssafy.enjoytrip.core.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.PlaceMapPin;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 지도 키워드 검색을 위한 오케스트레이션 서비스.
 * 장소(관광지) 정보 및 쪽지 정보를 키워드로 통합 검색하여 하나의 List<MapPin>으로 조합 및 정렬을 수행한다.
 * 서비스 간의 직접 참조를 피하는 규칙이 있으나, 이 서비스는 제품 노출용 통합 검색 Use Case를
 * 소유한 명시적 Orchestrator로써 NoteService와 AttractionService를 임포트하여 조합한다.
 */
@Service
@RequiredArgsConstructor
public class MapSearchService {
    private final NoteService noteService;
    private final AttractionService attractionService;

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
            List<NearbyAttractionCandidate> places = attractionService.searchMapPlaces(
                    keyword,
                    escapedKeyword,
                    longitude,
                    latitude,
                    radius,
                    limit,
                    viewerMemberId
            );
            for (NearbyAttractionCandidate candidate : places) {
                int matchTier = candidate.attraction().title().equalsIgnoreCase(keyword) ? 0 : 1;
                merged.add(new PlaceMapPin(
                        candidate.attraction().id(),
                        candidate.attraction().title(),
                        candidate.attraction().addr1(),
                        candidate.attraction().latitude(),
                        candidate.attraction().longitude(),
                        candidate.attraction().firstImage(),
                        candidate.attraction().contentTypeId(),
                        candidate.distanceMeters(),
                        candidate.attraction().saved(),
                        candidate.attraction().saveCount(),
                        candidate.attraction().ratingAverage(),
                        candidate.attraction().ratingCount(),
                        matchTier
                ));
            }
        }

        // 2. 쪽지(NOTE) 검색
        if (target.includesNotes()) {
            List<NoteMapPin> notes = noteService.searchMapNotes(
                    keyword,
                    escapedKeyword,
                    longitude,
                    latitude,
                    radius,
                    noteCategory,
                    limit,
                    viewerMemberId
            );
            merged.addAll(notes);
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
