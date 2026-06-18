package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.PlanItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PlanReplaceItemsRequest(
        @NotNull List<@Valid PlanRouteItemRequest> routeItems
) {
    public List<PlanItem> toPlanItems(String planId) {
        return routeItems.stream()
                .map(routeItem -> routeItem.toPlanItem(planId))
                .toList();
    }
}
