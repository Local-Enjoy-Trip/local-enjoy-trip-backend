package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.RouteSummary;

public record RouteSummaryResponse(
        int stopCount,
        int segmentCount,
        int totalDurationSeconds,
        int totalDistanceMeters
) {
    public static RouteSummaryResponse from(RouteSummary summary) {
        return new RouteSummaryResponse(
                summary.stopCount(),
                summary.segmentCount(),
                summary.totalDurationSeconds(),
                summary.totalDistanceMeters()
        );
    }
}
