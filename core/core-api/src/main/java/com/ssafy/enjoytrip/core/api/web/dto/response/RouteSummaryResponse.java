package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Course;

public record RouteSummaryResponse(
        int stopCount,
        int segmentCount,
        int totalDurationSeconds,
        int totalDistanceMeters
) {
    public static RouteSummaryResponse from(Course course) {
        return new RouteSummaryResponse(
                course.stopCount(),
                course.segmentCount(),
                course.totalDurationSeconds(),
                course.totalDistanceMeters()
        );
    }
}
