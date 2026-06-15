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
    private static final double DEFAULT_RADIUS_METERS = 500.0;
    private static final int DEFAULT_LIMIT = 50;

    public MapExploreCommand(
            String viewerUserId,
            Double longitude,
            Double latitude,
            Double radiusMeters,
            Integer limit,
            MapExploreFilter filter,
            NoteCategory noteCategory
    ) {
        this(
                viewerUserId,
                longitude,
                latitude,
                radiusMeters == null ? DEFAULT_RADIUS_METERS : radiusMeters,
                limit == null ? DEFAULT_LIMIT : limit,
                filter == null ? MapExploreFilter.ALL : filter,
                noteCategory
        );
    }

    public boolean hasExplicitCoordinates() {
        return longitude != null && latitude != null;
    }
}
