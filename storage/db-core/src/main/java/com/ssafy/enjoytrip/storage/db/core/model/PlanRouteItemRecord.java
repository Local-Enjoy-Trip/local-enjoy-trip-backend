package com.ssafy.enjoytrip.storage.db.core.model;

public record PlanRouteItemRecord(
        Long routeItemId,
        Long attractionId,
        String routeId,
        int position,
        int day,
        String memo,
        int stayMinutes,
        AttractionRecord attraction
) {
}
