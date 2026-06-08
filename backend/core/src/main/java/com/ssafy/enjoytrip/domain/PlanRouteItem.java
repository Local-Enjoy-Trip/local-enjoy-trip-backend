package com.ssafy.enjoytrip.domain;

public record PlanRouteItem(
        Long routeItemId,
        Long attractionId,
        String routeId,
        int position,
        int day,
        String memo,
        int stayMinutes,
        Attraction attraction
) {
}
