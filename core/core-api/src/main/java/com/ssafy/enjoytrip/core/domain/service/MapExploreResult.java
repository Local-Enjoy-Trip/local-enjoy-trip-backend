package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import java.util.List;

public record MapExploreResult(
        MapCenter center,
        double radiusMeters,
        MapExploreFilter filter,
        List<PlaceMapPin> places,
        List<NoteMapPin> notes
) {
    public MapExploreResult {
        places = List.copyOf(places);
        notes = List.copyOf(notes);
    }
}
