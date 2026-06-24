package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.CourseStop;

public record CourseItemResponse(
        Long id,
        String itemType,
        Long attractionId,
        Long noteId,
        int position,
        String title,
        Integer distanceToNext,
        Integer durationToNext
) {
    public static CourseItemResponse from(CourseStop stop) {
        return new CourseItemResponse(
                stop.id(),
                stop.target().type().name(),
                stop.target().attractionIdOrNull(),
                stop.target().noteIdOrNull(),
                stop.position(),
                stop.title(),
                stop.distanceToNext(),
                stop.durationToNext()
        );
    }
}
