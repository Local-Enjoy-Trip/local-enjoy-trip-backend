package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseRoute;
import com.ssafy.enjoytrip.core.domain.CourseStatus;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Locale;

public record CourseUpdateRequest(
        @NotBlank String title,
        String regionName,
        String visibility,
        String status,
        String description,
        String coverImageUrl,
        List<@Valid CourseItemRequest> items
) {
    public Course toCourse(String id, Long ownerMemberId) {
        return new Course(
                id,
                ownerMemberId,
                title.strip(),
                blankToNull(regionName),
                defaultValue(visibility, "PRIVATE"),
                statusValue(status),
                blankToNull(description),
                blankToNull(coverImageUrl),
                null,
                null,
                false,
                0,
                "",
                "",
                route()
        );
    }

    private CourseRoute route() {
        if (items == null || items.size() < 2) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
        return CourseRoute.ofStops(items.stream()
                .map(CourseItemRequest::toStop)
                .toList());
    }

    private static String defaultValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.strip().toUpperCase(Locale.ROOT);
    }

    private static String statusValue(String value) {
        String normalized = defaultValue(value, "READY");
        try {
            CourseStatus.valueOf(normalized);
            return normalized;
        } catch (IllegalArgumentException exception) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}
