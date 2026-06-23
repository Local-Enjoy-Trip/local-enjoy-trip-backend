package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import java.util.Locale;

public record CourseRouteSegment(
        int segmentOrder,
        int fromPosition,
        int toPosition,
        String travelMode,
        int durationSeconds,
        int distanceMeters
) {
    public CourseRouteSegment {
        if (segmentOrder <= 0 || fromPosition <= 0 || toPosition <= 0) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
        if (durationSeconds < 0 || distanceMeters < 0 || travelMode == null || travelMode.isBlank()) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
        travelMode = travelMode.strip().toUpperCase(Locale.ROOT);
    }
}
