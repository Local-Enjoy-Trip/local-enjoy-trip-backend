package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

public record CourseItemRequest(
        @NotBlank String itemType,
        Long attractionId,
        Long noteId,
        Integer position,
        @Min(1) Integer day,
        String memo,
        @Min(1) Integer stayMinutes
) {
    public CourseStop toStop() {
        return new CourseStop(
                null,
                target(),
                1,
                day == null ? 1 : day,
                normalizedMemo(),
                stayMinutes,
                null
        );
    }

    private CourseStopTarget target() {
        String normalizedType = itemType.strip().toUpperCase(Locale.ROOT);
        if ("ATTRACTION".equals(normalizedType) && attractionId != null && noteId == null) {
            return CourseStopTarget.attraction(attractionId);
        }
        if ("NOTE".equals(normalizedType) && noteId != null && attractionId == null) {
            return CourseStopTarget.note(noteId);
        }
        throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
    }

    private String normalizedMemo() {
        if (memo == null || memo.isBlank()) {
            return null;
        }
        return memo.strip();
    }
}
