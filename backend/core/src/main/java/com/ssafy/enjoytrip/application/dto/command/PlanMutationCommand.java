package com.ssafy.enjoytrip.application.dto.command;

import java.util.List;

public record PlanMutationCommand(
        String id,
        String title,
        String startDate,
        String endDate,
        Integer budget,
        String note,
        List<PlanRouteItemCommand> routeItems
) {
}
