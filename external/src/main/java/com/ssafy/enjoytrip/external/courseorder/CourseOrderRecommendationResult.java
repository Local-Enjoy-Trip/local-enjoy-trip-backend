package com.ssafy.enjoytrip.external.courseorder;

import java.util.List;

public record CourseOrderRecommendationResult(
        List<Long> orderedItemIds,
        String diagnostics
) {
    public CourseOrderRecommendationResult {
        orderedItemIds = List.copyOf(orderedItemIds == null ? List.of() : orderedItemIds);
    }
}
