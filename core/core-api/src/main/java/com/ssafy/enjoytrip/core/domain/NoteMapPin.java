package com.ssafy.enjoytrip.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record NoteMapPin(
        Long id,
        String title,
        NoteCategory category,
        NoteVisibility visibility,
        Double latitude,
        Double longitude,
        String regionName,
        double distanceMeters,
        String imageObjectKey,
        String authorNickname,
        String authorProfileImageUrl,
        NoteViewerRelationship relationshipToViewer,
        LocalDateTime createdAt,
        int matchTier
) implements MapPin {
    @JsonProperty("type")
    @Override
    public String type() { return "NOTE"; }

    public NoteMapPin(Long id, String title, NoteCategory category, NoteVisibility visibility, Double latitude, Double longitude, String regionName, double distanceMeters, String imageObjectKey, String authorNickname, String authorProfileImageUrl, NoteViewerRelationship relationshipToViewer, LocalDateTime createdAt) {
        this(id, title, category, visibility, latitude, longitude, regionName, distanceMeters, imageObjectKey, authorNickname, authorProfileImageUrl, relationshipToViewer, createdAt, 0);
    }
}
