package com.ssafy.enjoytrip.external.courseorder;

public record CourseOrderRecommendationItem(
        Long id,
        String itemType,
        Long targetId,
        String title,
        int currentPosition,
        String contentTypeId,
        Double latitude,
        Double longitude
) {
    public CourseOrderRecommendationItem(Long id,
                                         String itemType,
                                         Long targetId,
                                         String title,
                                         int currentPosition,
                                         Double latitude,
                                         Double longitude) {
        this(id, itemType, targetId, title, currentPosition, null, latitude, longitude);
    }
}
