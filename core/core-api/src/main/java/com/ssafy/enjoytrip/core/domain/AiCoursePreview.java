package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public record AiCoursePreview(
        String title,
        String reason,
        List<Stop> stops,
        List<String> tags
) {
    public AiCoursePreview {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public record Stop(
            long attractionId,
            String title,
            String addr1,
            String firstImage
    ) {}
}
