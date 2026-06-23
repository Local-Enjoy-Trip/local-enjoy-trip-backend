package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public record CourseOrderPreview(
        Course course,
        List<CourseOrderPreviewItem> items
) {
    public CourseOrderPreview {
        items = List.copyOf(items == null ? List.of() : items);
    }

    public boolean hasCompleteCoordinates() {
        return items.stream()
                .allMatch(item -> isFiniteCoordinate(item.latitude(), item.longitude()));
    }

    public int itemCount() {
        return items.size();
    }

    private static boolean isFiniteCoordinate(Double latitude, Double longitude) {
        return latitude != null
                && longitude != null
                && Double.isFinite(latitude)
                && Double.isFinite(longitude);
    }
}
