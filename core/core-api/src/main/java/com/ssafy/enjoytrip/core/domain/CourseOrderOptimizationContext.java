package com.ssafy.enjoytrip.core.domain;

public record CourseOrderOptimizationContext(
        Double currentLatitude,
        Double currentLongitude
) {
    public static CourseOrderOptimizationContext empty() {
        return new CourseOrderOptimizationContext(null, null);
    }
}
