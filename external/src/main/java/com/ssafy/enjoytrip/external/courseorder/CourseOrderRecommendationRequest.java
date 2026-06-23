package com.ssafy.enjoytrip.external.courseorder;

import java.util.List;

public record CourseOrderRecommendationRequest(
        String courseId,
        Double currentLatitude,
        Double currentLongitude,
        List<CourseOrderRecommendationItem> items
) {
    public CourseOrderRecommendationRequest(String courseId, List<CourseOrderRecommendationItem> items) {
        this(courseId, null, null, items);
    }

    public CourseOrderRecommendationRequest {
        items = List.copyOf(items == null ? List.of() : items);
    }
}
