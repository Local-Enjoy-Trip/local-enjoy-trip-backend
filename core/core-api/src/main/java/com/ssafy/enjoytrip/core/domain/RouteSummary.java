package com.ssafy.enjoytrip.core.domain;

public record RouteSummary(
        int stopCount,
        int segmentCount,
        int totalDurationSeconds,
        int totalDistanceMeters
) {
    public static RouteSummary empty() {
        return new RouteSummary(0, 0, 0, 0);
    }
}
