package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public class DefaultCourseRoutePlanner implements CourseRoutePlanner {
    private static final double WALKING_METERS_PER_SECOND = 1.4;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    @Override
    public List<CourseStop> plan(List<CourseStopPoint> points) {
        List<CourseStopPoint> normalizedPoints = normalize(points);
        requireCoordinates(normalizedPoints);

        if (normalizedPoints.size() < 2) {
            return normalizedPoints.stream()
                    .map(CourseStopPoint::stop)
                    .toList();
        }

        return IntStream.range(0, normalizedPoints.size())
                .mapToObj(index -> stopWithMetrics(normalizedPoints, index))
                .toList();
    }

    private static List<CourseStopPoint> normalize(List<CourseStopPoint> points) {
        return IntStream.range(0, points.size())
                .mapToObj(index -> {
                    CourseStopPoint point = points.get(index);
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

    private static CourseStop stopWithMetrics(List<CourseStopPoint> points, int index) {
        CourseStop stop = points.get(index).stop();

        if (index == points.size() - 1) {
            return stop;
        }

        int distanceMeters = distanceMeters(
                points.get(index).latitude(), points.get(index).longitude(),
                points.get(index + 1).latitude(), points.get(index + 1).longitude()
        );
        int durationSeconds = durationSeconds(distanceMeters);

        return stop.withNextMetrics(distanceMeters, durationSeconds);
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
