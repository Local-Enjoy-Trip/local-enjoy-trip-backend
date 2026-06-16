package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.MEMBER_REPRESENTATIVE_LOCATION_REQUIRED;
import static com.ssafy.enjoytrip.support.error.ErrorType.USER_NOT_FOUND;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.domain.MapCenter;
import com.ssafy.enjoytrip.dto.query.MapExploreCommand;
import com.ssafy.enjoytrip.domain.MapExploreResult;
import com.ssafy.enjoytrip.dto.query.MapNotesCondition;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.dto.query.NearbySearchCondition;
import com.ssafy.enjoytrip.domain.NoteMapPin;
import com.ssafy.enjoytrip.domain.PlaceMapPin;
import com.ssafy.enjoytrip.repository.AttractionRepository;
import com.ssafy.enjoytrip.repository.MemberRepository;
import com.ssafy.enjoytrip.repository.NoteRepository;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapExploreService {
    private final MemberRepository memberRepository;
    private final AttractionRepository attractionRepository;
    private final NoteRepository noteRepository;

    public MapExploreResult explore(MapExploreCommand command) {
        Member viewer = findViewer(command.viewerUserId());
        MapCenter center = resolveMapCenter(command, viewer);
        List<PlaceMapPin> places = findPlacePins(command, center);
        List<NoteMapPin> notes = findNotePins(command, center);

        return new MapExploreResult(
                center,
                command.radiusMeters(),
                command.limit(),
                command.filter(),
                places,
                notes
        );
    }

    private Member findViewer(String viewerUserId) {
        Member viewer = memberRepository.findByUserId(viewerUserId);
        if (viewer == null) {
            throw new CoreException(USER_NOT_FOUND);
        }

        return viewer;
    }

    private MapCenter resolveMapCenter(MapExploreCommand command, Member viewer) {
        if (command.hasExplicitCoordinates()) {
            return new MapCenter(command.longitude(), command.latitude(), null, false);
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

    private List<PlaceMapPin> findPlacePins(MapExploreCommand command, MapCenter center) {
        if (!command.filter().includesPlaces()) {
            return List.of();
        }

        return attractionRepository.findNearbyCandidates(
                        new NearbySearchCondition(
                                center.longitude(),
                                center.latitude(),
                                command.radiusMeters(),
                                command.limit()
                        ),
                        command.viewerUserId()
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
                attraction.ratingAverage(),
                attraction.ratingCount()
        );
    }

    private List<NoteMapPin> findNotePins(MapExploreCommand command, MapCenter center) {
        if (!command.filter().includesNotes()) {
            return List.of();
        }

        return noteRepository.findMapNotes(new MapNotesCondition(
                center.longitude(),
                center.latitude(),
                command.radiusMeters(),
                command.limit(),
                command.viewerUserId(),
                command.noteCategory(),
                command.filter().friendNotesOnly()
        ));
    }

}
