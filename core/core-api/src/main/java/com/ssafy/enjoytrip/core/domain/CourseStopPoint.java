package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;

public record CourseStopPoint(
        CourseStop stop,
        String title,
        Double latitude,
        Double longitude
) {
    public CourseStopPoint {
        if (stop == null) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
    }
}
