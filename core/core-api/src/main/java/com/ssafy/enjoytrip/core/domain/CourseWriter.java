package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_NOT_FOUND;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRouteSegmentRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CourseWriter {
    private final CourseMapper courseMapper;
    private final CourseStopPointResolver courseStopPointResolver;
    private final CourseRoutePlanner courseRoutePlanner;

    @Transactional
    public Course create(Course course) {
        return create(course, planRoute(course.route()));
    }

    @Transactional
    public Course update(Course course) {
        return update(course, planRoute(course.route()));
    }

    @Transactional
    public void deleteOwned(String courseId, String ownerUserId) {
        if (courseMapper.softDeleteOwned(courseId, ownerUserId) <= 0) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
    }

    private Course create(Course course, PlannedCourseRoute plannedRoute) {
        requireCompleteSegmentsWhenPresent(plannedRoute.route());
        courseMapper.insert(toRecord(course));
        updateStartLocation(course.id(), plannedRoute.startPoint());
        return course.withStartLocation(plannedRoute.startPoint())
                .withRoute(saveRoute(course.id(), plannedRoute.route()));
    }

    private Course update(Course course, PlannedCourseRoute plannedRoute) {
        requireCompleteSegmentsWhenPresent(plannedRoute.route());
        if (courseMapper.updateOwned(toRecord(course)) <= 0) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
        updateStartLocation(course.id(), plannedRoute.startPoint());
        return course.withStartLocation(plannedRoute.startPoint())
                .withRoute(replaceRouteRows(course.id(), plannedRoute.route()));
    }

    private PlannedCourseRoute planRoute(CourseRoute route) {
        List<CourseStopPoint> points = courseStopPointResolver.resolveAll(route.stops());
        CourseRoute plannedRoute = courseRoutePlanner.plan(points);
        requireCompleteSegmentsWhenPresent(plannedRoute);
        return new PlannedCourseRoute(plannedRoute, firstPoint(points));
    }

    private void updateStartLocation(String courseId, CourseStopPoint startPoint) {
        Double longitude = startPoint == null ? null : startPoint.longitude();
        Double latitude = startPoint == null ? null : startPoint.latitude();
        if (courseMapper.updateStartLocation(courseId, longitude, latitude) <= 0) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
    }

    private static CourseStopPoint firstPoint(List<CourseStopPoint> points) {
        if (points.isEmpty()) {
            return null;
        }
        return points.get(0);
    }

    private CourseRoute replaceRouteRows(String courseId, CourseRoute route) {
        courseMapper.deleteSegmentsByCourseId(courseId);
        courseMapper.deleteItemsByCourseId(courseId);
        return saveRoute(courseId, route);
    }

    private CourseRoute saveRoute(String courseId, CourseRoute route) {
        List<CourseStop> savedStops = saveStops(courseId, route.stops());
        saveSegments(courseId, route.segments(), itemIdsByPosition(savedStops));
        return savedRoute(savedStops, route.segments());
    }

    private List<CourseStop> saveStops(String courseId, List<CourseStop> stops) {
        if (stops.isEmpty()) {
            return List.of();
        }

        List<CourseItemRecord> records = stops.stream()
                .map(stop -> toItemRecord(courseId, stop))
                .toList();
        if (courseMapper.insertItems(records) != records.size()) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }
        return savedStops(stops, records);
    }

    private void saveSegments(String courseId,
                              List<CourseRouteSegment> segments,
                              Map<Integer, Long> itemIdsByPosition) {
        if (segments.isEmpty()) {
            return;
        }

        List<CourseRouteSegmentRecord> records = segments.stream()
                .map(segment -> toSegmentRecord(courseId, segment, itemIdsByPosition))
                .toList();
        if (courseMapper.insertSegments(records) != records.size()) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }
    }

    private static List<CourseStop> savedStops(List<CourseStop> stops, List<CourseItemRecord> records) {
        List<CourseStop> savedStops = new ArrayList<>();
        for (int index = 0; index < stops.size(); index++) {
            CourseItemRecord record = records.get(index);
            if (record.getId() == null) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
            savedStops.add(stops.get(index).withId(record.getId()));
        }
        return savedStops;
    }

    private static Map<Integer, Long> itemIdsByPosition(List<CourseStop> savedStops) {
        Map<Integer, Long> itemIdsByPosition = new HashMap<>();
        for (CourseStop stop : savedStops) {
            if (stop.id() == null) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
            Long previous = itemIdsByPosition.put(stop.position(), stop.id());
            if (previous != null) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
        }
        requireCompleteItemIds(itemIdsByPosition, savedStops.size());
        return itemIdsByPosition;
    }

    private static CourseRoute savedRoute(List<CourseStop> savedStops, List<CourseRouteSegment> segments) {
        if (segments.isEmpty()) {
            return CourseRoute.ofStops(savedStops);
        }
        return CourseRoute.planned(savedStops, segments);
    }

    private static void requireCompleteItemIds(Map<Integer, Long> itemIdsByPosition, int expectedStopCount) {
        for (int position = 1; position <= expectedStopCount; position++) {
            if (!itemIdsByPosition.containsKey(position)) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
        }
    }

    private static CourseItemRecord toItemRecord(String courseId, CourseStop stop) {
        return new CourseItemRecord(
                courseId,
                stop.target().type().name(),
                stop.target().attractionIdOrNull(),
                stop.target().noteIdOrNull(),
                stop.position(),
                stop.day(),
                stop.memo(),
                stop.stayMinutes()
        );
    }

    private static CourseRouteSegmentRecord toSegmentRecord(String courseId,
                                                            CourseRouteSegment segment,
                                                            Map<Integer, Long> itemIdsByPosition) {
        Long fromItemId = itemIdsByPosition.get(segment.fromPosition());
        Long toItemId = itemIdsByPosition.get(segment.toPosition());
        if (fromItemId == null || toItemId == null) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }
        return new CourseRouteSegmentRecord(
                courseId,
                fromItemId,
                toItemId,
                segment.segmentOrder(),
                segment.travelMode(),
                segment.durationSeconds(),
                segment.distanceMeters()
        );
    }

    private static CourseRecord toRecord(Course course) {
        return new CourseRecord(
                course.id(),
                course.ownerUserId(),
                course.title(),
                course.regionName(),
                course.visibility(),
                course.status(),
                course.description(),
                course.coverImageUrl(),
                course.curationSection(),
                course.curationOrder()
        );
    }

    private static void requireCompleteSegmentsWhenPresent(CourseRoute route) {
        if (!route.segments().isEmpty() && !hasCompleteSegmentSet(route)) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }
    }

    private static boolean hasCompleteSegmentSet(CourseRoute route) {
        if (route.stops().size() < 2) {
            return route.segments().isEmpty();
        }
        return route.segments().size() == route.stops().size() - 1;
    }

    private record PlannedCourseRoute(
            CourseRoute route,
            CourseStopPoint startPoint
    ) {
    }
}
