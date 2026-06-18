package com.ssafy.enjoytrip.core.api.web.dto.response;

import java.util.List;

public record RouteSplitByDayResponse(List<List<Integer>> days, List<Double> dayDistanceKm) {
}
