package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public record MapExploreResult(
        MapCenter center,
        double radiusMeters,
        int limit,
        MapExploreFilter filter,
        List<PlaceMapPin> places,
        List<NoteMapPin> notes
) {
    public MapExploreResult {
        places = List.copyOf(places);
        notes = List.copyOf(notes);
    }
}
