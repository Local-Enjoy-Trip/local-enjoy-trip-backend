package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;

public record CourseStop(
        Long id,
        CourseStopTarget target,
        int position,
        int day,
        String memo,
        Integer stayMinutes,
        String title
) {
    public CourseStop {
        if (target == null || position <= 0 || day <= 0 || invalidStayMinutes(stayMinutes)) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
    }

    public CourseStop withId(Long nextId) {
        return new CourseStop(nextId, target, position, day, memo, stayMinutes, title);
    }

    public CourseStop withPosition(int nextPosition) {
        return new CourseStop(id, target, nextPosition, day, memo, stayMinutes, title);
    }

    public CourseStop withTitle(String nextTitle) {
        return new CourseStop(id, target, position, day, memo, stayMinutes, nextTitle);
    }

    public CourseStop withoutStorageId() {
        return withId(null);
    }

    private static boolean invalidStayMinutes(Integer stayMinutes) {
        return stayMinutes != null && stayMinutes <= 0;
    }
}
