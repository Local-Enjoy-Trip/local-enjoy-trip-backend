package com.ssafy.enjoytrip.external.aicourse;

import java.util.List;

public record AiCourseGenerationResult(
        String title,
        List<Long> attractionIds,
        String reason,
        List<String> tags
) {
    public AiCourseGenerationResult {
        attractionIds = attractionIds == null ? List.of() : List.copyOf(attractionIds);
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
