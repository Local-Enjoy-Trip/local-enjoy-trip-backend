package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.core.domain.MapCenter;
import com.ssafy.enjoytrip.core.domain.MapExploreResult;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.PlaceMapPin;
import com.ssafy.enjoytrip.core.domain.query.MapNotesCondition;
import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapExploreService {
    private final AttractionService attractionService;
    private final NoteService noteService;

    public MapExploreResult explore(
            String viewerUserId,
            double longitude,
            double latitude,
            double radiusMeters,
            MapExploreFilter filter,
            NoteCategory noteCategory
    ) {
        MapCenter center = new MapCenter(longitude, latitude, null);
        List<PlaceMapPin> places = findPlacePins(viewerUserId, radiusMeters, filter, center);
        List<NoteMapPin> notes = findNotePins(
                viewerUserId,
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
            String viewerUserId,
            double radiusMeters,
            MapExploreFilter filter,
            MapCenter center
    ) {
        if (!filter.includesPlaces()) {
            return List.of();
        }

        return attractionService.findNearbyCandidates(
                        new DistanceSearchCondition(
                                center.longitude(),
                                center.latitude(),
                                null,
                                radiusMeters
                        ),
                        viewerUserId,
                        filter.savedPlacesOnly()
                )
                .stream()
                .map(MapExploreService::toPlacePin)
                .toList();
    }

    private static PlaceMapPin toPlacePin(NearbyAttractionCandidate candidate) {
        Attraction attraction = candidate.attraction();

        return new PlaceMapPin(
                attraction.id(),
                attraction.title(),
                attraction.addr1(),
                attraction.latitude(),
                attraction.longitude(),
                attraction.primaryImageUrl(),
                attraction.contentTypeId(),
                candidate.distanceMeters(),
                attraction.saved(),
                attraction.saveCount(),
                attraction.ratingAverage(),
                attraction.ratingCount()
        );
    }

    private List<NoteMapPin> findNotePins(
            String viewerUserId,
            double radiusMeters,
            MapExploreFilter filter,
            NoteCategory noteCategory,
            MapCenter center
    ) {
        if (!filter.includesNotes()) {
            return List.of();
        }

        return noteService.findMapNotes(new MapNotesCondition(
                center.longitude(),
                center.latitude(),
                radiusMeters,
                null,
                viewerUserId,
                noteCategory,
                filter.friendNotesOnly()
        ));
    }
}
