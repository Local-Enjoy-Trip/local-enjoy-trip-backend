package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.MapCenter;
import com.ssafy.enjoytrip.core.domain.MapExploreResult;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.PlaceMapPin;
import java.util.List;

public record MapExploreResponse(
        MapCenter center,
        double radiusMeters,
        MapExploreFilter filter,
        List<PlaceMapPin> places,
        List<NoteMapPin> notes
) {
    public MapExploreResponse {
        places = List.copyOf(places);
        notes = List.copyOf(notes);
    }

    public static MapExploreResponse from(MapExploreResult result) {
        return new MapExploreResponse(
                result.center(),
                result.radiusMeters(),
                result.filter(),
                result.places(),
                result.notes()
        );
    }
}
