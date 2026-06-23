package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record CourseRoute(
        List<CourseStop> stops,
        List<CourseRouteSegment> segments
) {
    public CourseRoute {
        stops = List.copyOf(stops == null ? List.of() : stops);
        segments = List.copyOf(segments == null ? List.of() : segments);
        validateStops(stops);
        validateSegments(stops, segments);
    }

    public static CourseRoute empty() {
        return new CourseRoute(List.of(), List.of());
    }

    public static CourseRoute ofStops(List<CourseStop> stops) {
        return new CourseRoute(renormalize(stops == null ? List.of() : stops), List.of());
    }

    public static CourseRoute planned(List<CourseStop> stops, List<CourseRouteSegment> segments) {
        return new CourseRoute(stops, segments);
    }

    public CourseRoute add(CourseStop stop) {
        List<CourseStop> nextStops = new ArrayList<>(stops);
        nextStops.add(stop.withoutStorageId().withPosition(nextStops.size() + 1));
        return CourseRoute.ofStops(nextStops);
    }

    public CourseRoute removePosition(int position) {
        return CourseRoute.ofStops(stops.stream()
                .filter(stop -> stop.position() != position)
                .toList());
    }

    public CourseRoute reorder(List<Integer> orderedPositions) {
        Map<Integer, CourseStop> byPosition = stops.stream()
                .collect(Collectors.toMap(CourseStop::position, Function.identity()));
        if (orderedPositions.size() != stops.size() || !byPosition.keySet().containsAll(orderedPositions)) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }

        List<CourseStop> reorderedStops = orderedPositions.stream()
                .map(byPosition::get)
                .toList();
        return CourseRoute.ofStops(reorderedStops);
    }

    public RouteSummary summary() {
        if (stops.isEmpty()) {
            return RouteSummary.empty();
        }

        int totalDurationSeconds = segments.stream()
                .mapToInt(CourseRouteSegment::durationSeconds)
                .sum();
        int totalDistanceMeters = segments.stream()
                .mapToInt(CourseRouteSegment::distanceMeters)
                .sum();
        return new RouteSummary(
                stops.size(),
                segments.size(),
                totalDurationSeconds,
                totalDistanceMeters
        );
    }

    private static List<CourseStop> renormalize(List<CourseStop> stops) {
        return IntStream.range(0, stops.size())
                .mapToObj(index -> stops.get(index).withPosition(index + 1))
                .toList();
    }

    private static void validateStops(List<CourseStop> stops) {
        for (int index = 0; index < stops.size(); index++) {
            if (stops.get(index).position() != index + 1) {
                throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
            }
        }
    }

    private static void validateSegments(List<CourseStop> stops, List<CourseRouteSegment> segments) {
        if (stops.size() < 2 && !segments.isEmpty()) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
        if (segments.isEmpty()) {
            return;
        }
        if (segments.size() != stops.size() - 1) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }

        for (int index = 0; index < segments.size(); index++) {
            CourseRouteSegment segment = segments.get(index);
            validateSegment(index, segment);
        }
    }

    private static void validateSegment(int index, CourseRouteSegment segment) {
        int expectedOrder = index + 1;
        if (segment.segmentOrder() != expectedOrder
                || segment.fromPosition() != expectedOrder
                || segment.toPosition() != expectedOrder + 1) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
    }
}
