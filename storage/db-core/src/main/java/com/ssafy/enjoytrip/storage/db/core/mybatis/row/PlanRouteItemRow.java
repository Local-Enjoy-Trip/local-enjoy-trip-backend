package com.ssafy.enjoytrip.storage.db.core.mybatis.row;

public record PlanRouteItemRow(
        Long routeItemId,
        Long attractionId,
        String routeId,
        int position,
        int day,
        String memo,
        int stayMinutes,
        AttractionRow attraction
) {
}
