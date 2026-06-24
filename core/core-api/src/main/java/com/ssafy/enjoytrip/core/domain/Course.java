package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import java.util.List;

public record Course(
        String id,
        Long ownerMemberId,
        String title,
        String regionName,
        String date,
        boolean createdByAdmin,
        Coordinate startLocation,
        Double distanceMeters,
        int saveCount,
        String createdAt,
        String updatedAt,
        List<CourseStop> stops,
        List<CourseTag> tags
) {
    public Course {
        stops = List.copyOf(stops == null ? List.of() : stops);
        tags = List.copyOf(tags == null ? List.of() : tags);
    }

    public void requireOwnedBy(Long memberId) {
        if (!ownerMemberId.equals(memberId)) {
            throw new CoreException(ErrorType.COURSE_ACCESS_DENIED);
        }
    }

    public List<CourseStop> items() {
        return stops;
    }

    public Course withStops(List<CourseStop> nextStops) {
        return new Course(
                id,
                ownerMemberId,
                title,
                regionName,
                date,
                createdByAdmin,
                startLocation,
                distanceMeters,
                saveCount,
                createdAt,
                updatedAt,
                nextStops,
                tags
        );
    }

    public Course withStartLocation(CourseStopPoint startPoint) {
        Coordinate location = startPoint == null
                ? null
                : new Coordinate(startPoint.latitude(), startPoint.longitude());
        return new Course(
                id,
                ownerMemberId,
                title,
                regionName,
                date,
                createdByAdmin,
                location,
                distanceMeters,
                saveCount,
                createdAt,
                updatedAt,
                stops,
                tags
        );
    }

    public int stopCount() {
        return stops.size();
    }

    public int segmentCount() {
        return Math.max(stops.size() - 1, 0);
    }

    public int totalDistanceMeters() {
        return stops.stream()
                .mapToInt(stop -> stop.distanceToNext() == null ? 0 : stop.distanceToNext())
                .sum();
    }

    public int totalDurationSeconds() {
        return stops.stream()
                .mapToInt(stop -> stop.durationToNext() == null ? 0 : stop.durationToNext())
                .sum();
    }
}
