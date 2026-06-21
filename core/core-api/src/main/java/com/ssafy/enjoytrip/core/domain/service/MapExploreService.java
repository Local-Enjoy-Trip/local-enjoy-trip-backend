package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.MEMBER_REPRESENTATIVE_LOCATION_REQUIRED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.MapCenter;
import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.MapExploreResult;
import com.ssafy.enjoytrip.core.domain.query.MapNotesCondition;
import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.core.domain.query.NearbySearchCondition;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.PlaceMapPin;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapExploreService {
    private final MemberService memberService;
    private final AttractionService attractionService;
    private final NoteService noteService;

    public MapExploreResult explore(
            String viewerUserId,
            Double longitude,
            Double latitude,
            double radiusMeters,
            int limit,
            MapExploreFilter filter,
            NoteCategory noteCategory
    ) {
        Member viewer = findViewer(viewerUserId);
        MapCenter center = resolveMapCenter(longitude, latitude, viewer);
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

    private Member findViewer(String viewerUserId) {
        Member viewer = memberService.findByUserId(viewerUserId);
        if (viewer == null) {
            throw new CoreException(USER_NOT_FOUND);
        }

        return viewer;
    }

    private MapCenter resolveMapCenter(Double longitude, Double latitude, Member viewer) {
        if (longitude != null && latitude != null) {
            return new MapCenter(longitude, latitude, null, false);
        }

        if (viewer.representativeLatitude() == null || viewer.representativeLongitude() == null) {
            throw new CoreException(MEMBER_REPRESENTATIVE_LOCATION_REQUIRED);
        }

        return new MapCenter(
                viewer.representativeLongitude(),
                viewer.representativeLatitude(),
                viewer.representativeRegionName(),
                true
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
