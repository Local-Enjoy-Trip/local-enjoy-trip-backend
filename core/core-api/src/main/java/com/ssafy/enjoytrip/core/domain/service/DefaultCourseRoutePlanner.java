package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.CourseRoute;
import com.ssafy.enjoytrip.core.domain.CourseRouteSegment;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopPoint;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public class DefaultCourseRoutePlanner implements CourseRoutePlanner {
    private static final String DEFAULT_TRAVEL_MODE = "WALK";
    private static final double WALKING_METERS_PER_SECOND = 1.4;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    @Override
    public CourseRoute plan(List<CourseStopPoint> points) {
        List<CourseStopPoint> normalizedPoints = normalize(points);
        requireCoordinates(normalizedPoints);
        if (normalizedPoints.size() < 2) {
            return CourseRoute.ofStops(normalizedPoints.stream()
                    .map(CourseStopPoint::stop)
                    .toList());
        }
        List<CourseStop> stops = normalizedPoints.stream()
                .map(CourseStopPoint::stop)
                .toList();
        List<CourseRouteSegment> segments = IntStream.range(0, normalizedPoints.size() - 1)
                .mapToObj(index -> toSegment(
                        index,
                        normalizedPoints.get(index),
                        normalizedPoints.get(index + 1)
                ))
                .toList();
        return CourseRoute.planned(stops, segments);
    }

    private static List<CourseStopPoint> normalize(List<CourseStopPoint> points) {
        List<CourseStopPoint> sortedPoints = points.stream()
                .sorted(Comparator.comparingInt(point -> point.stop().position()))
                .toList();
        return IntStream.range(0, sortedPoints.size())
                .mapToObj(index -> {
                    CourseStopPoint point = sortedPoints.get(index);
                    return new CourseStopPoint(
                            point.stop().withPosition(index + 1),
                            point.title(),
                            point.latitude(),
                            point.longitude()
                    );
                })
                .toList();
    }

    private static void requireCoordinates(List<CourseStopPoint> points) {
        boolean missingCoordinate = points.stream()
                .anyMatch(point -> point.latitude() == null || point.longitude() == null);
        if (missingCoordinate) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
    }

    private static CourseRouteSegment toSegment(int index, CourseStopPoint from, CourseStopPoint to) {
        int distanceMeters = distanceMeters(from.latitude(), from.longitude(), to.latitude(), to.longitude());
        int durationSeconds = durationSeconds(distanceMeters);
        return new CourseRouteSegment(
                index + 1,
                from.stop().position(),
                to.stop().position(),
                DEFAULT_TRAVEL_MODE,
                durationSeconds,
                distanceMeters
        );
    }

    private static int distanceMeters(double fromLatitude,
                                      double fromLongitude,
                                      double toLatitude,
                                      double toLongitude) {
        double fromRadians = Math.toRadians(fromLatitude);
        double toRadians = Math.toRadians(toLatitude);
        double latitudeDelta = Math.toRadians(toLatitude - fromLatitude);
        double longitudeDelta = Math.toRadians(toLongitude - fromLongitude);
        double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
                + Math.cos(fromRadians) * Math.cos(toRadians)
                * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(EARTH_RADIUS_METERS * c);
    }

    private static int durationSeconds(int distanceMeters) {
        return (int) Math.ceil(distanceMeters / WALKING_METERS_PER_SECOND);
    }
}
