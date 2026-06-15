package com.ssafy.enjoytrip.domain;

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
