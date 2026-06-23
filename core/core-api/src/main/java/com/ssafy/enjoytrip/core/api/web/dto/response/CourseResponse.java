package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Course;
import java.util.List;

public record CourseResponse(
        String id,
        String ownerUserId,
        String title,
        String regionName,
        String visibility,
        String status,
        String description,
        String coverImageUrl,
        String curationSection,
        Integer curationOrder,
        boolean createdByAdmin,
        Double distanceMeters,
        CourseStartLocationResponse startLocation,
        int saveCount,
        String createdAt,
        String updatedAt,
        RouteSummaryResponse routeSummary,
        List<CourseItemResponse> items,
        List<CourseSegmentResponse> segments
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.id(),
                course.ownerUserId(),
                course.title(),
                course.regionName(),
                course.visibility(),
                course.status(),
                course.description(),
                course.coverImageUrl(),
                course.curationSection(),
                course.curationOrder(),
                course.createdByAdmin(),
                course.distanceMeters(),
                startLocation(course),
                course.saveCount(),
                course.createdAt(),
                course.updatedAt(),
                RouteSummaryResponse.from(course.routeSummary()),
                course.route().stops().stream()
                        .map(CourseItemResponse::from)
                        .toList(),
                course.route().segments().stream()
                        .map(CourseSegmentResponse::from)
                        .toList()
        );
    }

    private static CourseStartLocationResponse startLocation(Course course) {
        if (course.startLatitude() == null || course.startLongitude() == null) {
            return null;
        }
        return new CourseStartLocationResponse(course.startLongitude(), course.startLatitude());
    }

    public record CourseStartLocationResponse(
            Double longitude,
            Double latitude
    ) {
    }
}
