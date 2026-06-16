package com.ssafy.enjoytrip.dto.command;

public record PlanRouteItemCommand(
        Long attractionId,
        Integer day,
        String memo,
        Integer stayMinutes
) {
}
