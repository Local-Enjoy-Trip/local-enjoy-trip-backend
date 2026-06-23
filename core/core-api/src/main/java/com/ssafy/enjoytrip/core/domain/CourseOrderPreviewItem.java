package com.ssafy.enjoytrip.core.domain;


public record CourseOrderPreviewItem(
        CourseStop stop,
        String title,
        Double latitude,
        Double longitude,
        String contentTypeId
) {
    public CourseOrderPreviewItem(CourseStop stop, String title, Double latitude, Double longitude) {
        this(stop, title, latitude, longitude, null);
    }

    public Long id() {
        return stop.id();
    }

    public int day() {
        return stop.day();
    }

    public int position() {
        return stop.position();
    }
}
