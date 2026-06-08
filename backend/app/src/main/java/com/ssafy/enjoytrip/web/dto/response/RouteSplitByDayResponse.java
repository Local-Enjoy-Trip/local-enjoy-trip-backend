package com.ssafy.enjoytrip.web.dto.response;

import java.util.List;

public record RouteSplitByDayResponse(List<List<Integer>> days, List<Double> dayDistanceKm) {
}
