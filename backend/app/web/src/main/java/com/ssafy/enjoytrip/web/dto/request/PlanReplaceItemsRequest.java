package com.ssafy.enjoytrip.web.dto.request;

import com.ssafy.enjoytrip.dto.command.PlanRouteItemCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PlanReplaceItemsRequest(
        @NotNull List<@Valid PlanRouteItemRequest> routeItems
) {
    public List<PlanRouteItemCommand> toCommands() {
        return routeItems.stream().map(PlanRouteItemRequest::toCommand).toList();
    }
}
