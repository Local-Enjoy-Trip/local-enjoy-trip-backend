package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public record AiCoursePreview(
        String title,
        String reason,
        List<Stop> stops
) {
    public record Stop(
            long attractionId,
            String title,
            String addr1,
            String firstImage
    ) {}
}
