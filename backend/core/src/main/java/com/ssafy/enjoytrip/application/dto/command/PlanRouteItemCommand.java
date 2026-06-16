package com.ssafy.enjoytrip.application.dto.command;

public record PlanRouteItemCommand(
        Long attractionId,
        Integer day,
        String memo,
        Integer stayMinutes
) {
}
