package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.query.MapNotesCondition;
import com.ssafy.enjoytrip.core.domain.query.NearbySearchCondition;
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
            int limit,
            MapExploreFilter filter,
            NoteCategory noteCategory
    ) {
        MapCenter center = new MapCenter(longitude, latitude, null);
        List<PlaceMapPin> places = findPlacePins(viewerUserId, radiusMeters, limit, filter, center);
        List<NoteMapPin> notes = findNotePins(
                viewerUserId,
                radiusMeters,
                limit,
                filter,
                noteCategory,
                center
        );

        return new MapExploreResult(
                center,
                radiusMeters,
                limit,
                filter,
                places,
                notes
        );
    }

    private List<PlaceMapPin> findPlacePins(
            String viewerUserId,
            double radiusMeters,
            int limit,
            MapExploreFilter filter,
            MapCenter center
    ) {
        if (!filter.includesPlaces()) {
            return List.of();
        }

        return attractionService.findNearbyCandidates(
                        new NearbySearchCondition(
                                center.longitude(),
                                center.latitude(),
                                radiusMeters,
                                limit
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
                attraction.favorited(),
                attraction.saved(),
                attraction.saveCount(),
                attraction.ratingAverage(),
                attraction.ratingCount()
        );
    }

    private List<NoteMapPin> findNotePins(
            String viewerUserId,
            double radiusMeters,
            int limit,
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
                limit,
                viewerUserId,
                noteCategory,
                filter.friendNotesOnly()
        ));
    }
}
