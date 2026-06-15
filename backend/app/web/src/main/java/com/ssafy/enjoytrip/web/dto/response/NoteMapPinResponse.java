package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.NoteCategory;
import com.ssafy.enjoytrip.domain.NoteMapPin;
import com.ssafy.enjoytrip.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.domain.NoteVisibility;
import java.time.LocalDateTime;

public record NoteMapPinResponse(
        Long id,
        String title,
        NoteCategory category,
        NoteVisibility visibility,
        Double latitude,
        Double longitude,
        String regionName,
        double distanceMeters,
        String imageObjectKey,
        String authorUserId,
        String authorNickname,
        String authorProfileImageUrl,
        NoteViewerRelationship relationshipToViewer,
        LocalDateTime createdAt
) {
    public static NoteMapPinResponse from(NoteMapPin pin) {
        return new NoteMapPinResponse(
                pin.id(),
                pin.title(),
                pin.category(),
                pin.visibility(),
                pin.latitude(),
                pin.longitude(),
                pin.regionName(),
                pin.distanceMeters(),
                pin.imageObjectKey(),
                pin.authorUserId(),
                pin.authorNickname(),
                pin.authorProfileImageUrl(),
                pin.relationshipToViewer(),
                pin.createdAt()
        );
    }
}
