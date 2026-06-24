package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.MapCenter;
import com.ssafy.enjoytrip.core.domain.MapExploreResult;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.core.domain.PlaceMapPin;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 지도를 통한 주변 탐색(관광지 및 쪽지)을 수행하는 서비스.
 * 서비스 간의 결합도를 줄이기 위해 직접 Mappers에 접근하여 Read-Only 조회를 수행한다.
 */
@Service
@RequiredArgsConstructor
public class MapExploreService {
    private final AttractionMapper attractionMapper;
    private final NoteMapper noteMapper;

    public MapExploreResult explore(
            Long viewerMemberId,
            double longitude,
            double latitude,
            double radiusMeters,
            MapExploreFilter filter,
            NoteCategory noteCategory
    ) {
        MapCenter center = new MapCenter(longitude, latitude, null);
        List<PlaceMapPin> places = findPlacePins(viewerMemberId, radiusMeters, filter, center);
        List<NoteMapPin> notes = findNotePins(
                viewerMemberId,
                radiusMeters,
                filter,
                noteCategory,
                center
        );

        return new MapExploreResult(
                center,
                radiusMeters,
                filter,
                places,
                notes
        );
    }

    private List<PlaceMapPin> findPlacePins(
            Long viewerMemberId,
            double radiusMeters,
            MapExploreFilter filter,
            MapCenter center
    ) {
        if (!filter.includesPlaces()) {
            return List.of();
        }

        List<AttractionSearchRecord> records = attractionMapper.findNearby(
                center.longitude(),
                center.latitude(),
                radiusMeters,
                null,
                filter.savedPlacesOnly(),
                viewerMemberId
        );

        List<PlaceMapPin> pins = new ArrayList<>();
        Long currentId = null;
        for (AttractionSearchRecord r : records) {
            if (currentId != null && currentId.equals(r.id())) {
                continue;
            }
            currentId = r.id();
            pins.add(toPlacePin(r));
        }
        return pins;
    }

    private static PlaceMapPin toPlacePin(AttractionSearchRecord r) {
        return new PlaceMapPin(
                r.id(),
                r.title(),
                r.addr1(),
                r.latitude(),
                r.longitude(),
                r.firstImage(),
                r.contentTypeId(),
                r.distanceMeters() == null ? 0.0 : r.distanceMeters(),
                r.saved(),
                r.saveCount(),
                r.ratingAverage(),
                r.ratingCount()
        );
    }

    private List<NoteMapPin> findNotePins(
            Long viewerMemberId,
            double radiusMeters,
            MapExploreFilter filter,
            NoteCategory noteCategory,
            MapCenter center
    ) {
        if (!filter.includesNotes()) {
            return List.of();
        }

        List<NoteMapPinRecord> records = noteMapper.findMapPins(
                center.longitude(),
                center.latitude(),
                radiusMeters,
                null,
                viewerMemberId,
                noteCategory == null ? null : noteCategory.name(),
                filter.friendNotesOnly()
        );

        return records.stream()
                .map(r -> new NoteMapPin(
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
                        0 // matchTier defaults to 0 for explore
                ))
                .toList();
    }
}
