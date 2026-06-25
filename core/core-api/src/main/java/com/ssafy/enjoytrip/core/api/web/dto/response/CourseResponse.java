package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseTag;
import java.util.List;

public record CourseResponse(
        String id,
        String title,
        String regionName,
        String date,
        Double distanceMeters,
        CourseStartLocationResponse startLocation,
        int saveCount,
        String createdAt,
        String updatedAt,
        RouteSummaryResponse routeSummary,
        List<CourseItemResponse> items,
        List<TagResponse> tags
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.id(),
                course.title(),
                course.regionName(),
                course.date(),
                course.distanceMeters(),
                startLocation(course),
                course.saveCount(),
                course.createdAt(),
                course.updatedAt(),
                RouteSummaryResponse.from(course),
                course.stops().stream()
                        .map(CourseItemResponse::from)
                        .toList(),
                course.tags().stream()
                        .map(tag -> new TagResponse(tag.tagId(), tag.tagName()))
                        .toList()
        );
    }

    private static CourseStartLocationResponse startLocation(Course course) {
        if (course.startLocation() == null) {
            return null;
        }
        return new CourseStartLocationResponse(
                course.startLocation().longitude(),
                course.startLocation().latitude()
        );
    }

    public record CourseStartLocationResponse(
            Double longitude,
            Double latitude
    ) {
    }

    public record TagResponse(
            Long id,
            String name
    ) {
    }
}
