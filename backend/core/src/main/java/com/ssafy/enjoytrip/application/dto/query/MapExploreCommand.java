package com.ssafy.enjoytrip.application.dto.query;

import com.ssafy.enjoytrip.domain.MapExploreFilter;
import com.ssafy.enjoytrip.domain.NoteCategory;

public record MapExploreCommand(
        String viewerUserId,
        Double longitude,
        Double latitude,
        double radiusMeters,
        int limit,
        MapExploreFilter filter,
        NoteCategory noteCategory
) {
    public boolean hasExplicitCoordinates() {
        return longitude != null && latitude != null;
    }
}
