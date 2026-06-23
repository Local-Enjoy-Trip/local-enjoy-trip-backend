package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import java.util.List;

public record Course(
        String id,
        Long ownerMemberId,
        String title,
        String regionName,
        String visibility,
        String status,
        String description,
        String coverImageUrl,
        String curationSection,
        Integer curationOrder,
        boolean createdByAdmin,
        Double startLatitude,
        Double startLongitude,
        Double distanceMeters,
        int saveCount,
        String createdAt,
        String updatedAt,
        CourseRoute route
) {
    public Course {
        route = route == null ? CourseRoute.empty() : route;
    }

    public Course(String id,
                  Long ownerMemberId,
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
                  CourseRoute route) {
        this(
                id,
                ownerMemberId,
                title,
                regionName,
                visibility,
                status,
                description,
                coverImageUrl,
                curationSection,
                curationOrder,
                createdByAdmin,
                null,
                null,
                null,
                saveCount,
                createdAt,
                updatedAt,
                route
        );
    }

    public void requireOwnedBy(Long memberId) {
        if (!ownerMemberId.equals(memberId)) {
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
                ownerMemberId,
                title,
                regionName,
                visibility,
                status,
                description,
                coverImageUrl,
                curationSection,
                curationOrder,
                createdByAdmin,
                startLatitude,
                startLongitude,
                distanceMeters,
                saveCount,
                createdAt,
                updatedAt,
                nextRoute
        );
    }

    public Course withStartLocation(CourseStopPoint startPoint) {
        return new Course(
                id,
                ownerMemberId,
                title,
                regionName,
                visibility,
                status,
                description,
                coverImageUrl,
                curationSection,
                curationOrder,
                createdByAdmin,
                startPoint == null ? null : startPoint.latitude(),
                startPoint == null ? null : startPoint.longitude(),
                distanceMeters,
                saveCount,
                createdAt,
                updatedAt,
                route
        );
    }
}
