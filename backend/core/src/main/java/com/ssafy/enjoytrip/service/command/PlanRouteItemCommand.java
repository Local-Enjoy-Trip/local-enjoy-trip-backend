package com.ssafy.enjoytrip.service.command;

public record PlanRouteItemCommand(
        Long attractionId,
        Integer day,
        String memo,
        Integer stayMinutes
) {
}
