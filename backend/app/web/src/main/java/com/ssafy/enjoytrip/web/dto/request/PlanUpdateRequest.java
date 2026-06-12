package com.ssafy.enjoytrip.web.dto.request;

import com.ssafy.enjoytrip.service.command.PlanMutationCommand;
import com.ssafy.enjoytrip.service.command.PlanRouteItemCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record PlanUpdateRequest(
        String title,
        String startDate,
        String endDate,
        @PositiveOrZero Integer budget,
        String note,
        List<@Valid PlanRouteItemRequest> routeItems
) {
    public PlanMutationCommand toCommand() {
        return new PlanMutationCommand(
                null,
                title,
                startDate,
                endDate,
                budget,
                note,
                routeItems == null ? null : toRouteItemCommands(routeItems)
        );
    }

    private static List<PlanRouteItemCommand> toRouteItemCommands(List<PlanRouteItemRequest> routeItems) {
        return routeItems.stream().map(PlanRouteItemRequest::toCommand).toList();
    }
}
