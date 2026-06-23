package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import java.util.List;

public record Course(
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
        int saveCount,
        String createdAt,
        String updatedAt,
        CourseRoute route
) {
    public Course {
        route = route == null ? CourseRoute.empty() : route;
    }

    public void requireOwnedBy(String userId) {
        if (!ownerUserId.equals(userId)) {
            throw new CoreException(ErrorType.COURSE_ACCESS_DENIED);
        }
    }

    public List<CourseStop> items() {
        return route.stops();
    }

    public RouteSummary routeSummary() {
        return route.summary();
    }

    public Course withRoute(CourseRoute nextRoute) {
        return new Course(
                id,
                ownerUserId,
                title,
                regionName,
                visibility,
                status,
                description,
                coverImageUrl,
                curationSection,
                curationOrder,
                createdByAdmin,
                saveCount,
                createdAt,
                updatedAt,
                nextRoute
        );
    }
}
