package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.CourseRouteSegment;

public record CourseSegmentResponse(
        int segmentOrder,
        int fromPosition,
        int toPosition,
        String travelMode,
        int durationSeconds,
        int distanceMeters
) {
    public static CourseSegmentResponse from(CourseRouteSegment segment) {
        return new CourseSegmentResponse(
                segment.segmentOrder(),
                segment.fromPosition(),
                segment.toPosition(),
                segment.travelMode(),
                segment.durationSeconds(),
                segment.distanceMeters()
        );
    }
}
