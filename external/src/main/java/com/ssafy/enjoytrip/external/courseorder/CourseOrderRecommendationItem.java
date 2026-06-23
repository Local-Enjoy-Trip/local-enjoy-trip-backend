package com.ssafy.enjoytrip.external.courseorder;

public record CourseOrderRecommendationItem(
        Long id,
        String itemType,
        Long targetId,
        String title,
        int day,
        int currentPosition,
        Integer stayMinutes,
        String contentTypeId,
        Double latitude,
        Double longitude
) {
    public CourseOrderRecommendationItem(Long id,
                                         String itemType,
                                         Long targetId,
                                         String title,
                                         int day,
                                         int currentPosition,
                                         Double latitude,
                                         Double longitude) {
        this(id, itemType, targetId, title, day, currentPosition, null, null, latitude, longitude);
    }
}
