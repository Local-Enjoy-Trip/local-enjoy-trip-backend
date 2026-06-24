package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;

public record CourseStop(
        Long id,
        CourseStopTarget target,
        int position,
        String title,
        Integer distanceToNext,
        Integer durationToNext
) {
    public CourseStop {
        if (target == null || position <= 0) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
        if (invalidNextMetric(distanceToNext) || invalidNextMetric(durationToNext)) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
    }

    public CourseStop withId(Long nextId) {
        return new CourseStop(nextId, target, position, title, distanceToNext, durationToNext);
    }

    public CourseStop withPosition(int nextPosition) {
        return new CourseStop(id, target, nextPosition, title, distanceToNext, durationToNext);
    }

    public CourseStop withTitle(String nextTitle) {
        return new CourseStop(id, target, position, nextTitle, distanceToNext, durationToNext);
    }

    public CourseStop withNextMetrics(Integer nextDistanceToNext, Integer nextDurationToNext) {
        return new CourseStop(id, target, position, title, nextDistanceToNext, nextDurationToNext);
    }

    public CourseStop withoutStorageId() {
        return withId(null);
    }

    private static boolean invalidNextMetric(Integer metric) {
        return metric != null && metric < 0;
    }
}
