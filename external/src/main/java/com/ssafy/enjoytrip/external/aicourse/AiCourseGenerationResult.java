package com.ssafy.enjoytrip.external.aicourse;

import java.util.List;

public record AiCourseGenerationResult(
        String title,
        List<Long> attractionIds,
        String reason
) {
    public AiCourseGenerationResult {
        attractionIds = attractionIds == null ? List.of() : List.copyOf(attractionIds);
    }
}
