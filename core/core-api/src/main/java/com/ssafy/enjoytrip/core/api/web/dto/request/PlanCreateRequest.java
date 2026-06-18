package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.PlanItem;
import com.ssafy.enjoytrip.core.domain.TravelPlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record PlanCreateRequest(
        @NotBlank String id,
        @NotBlank String title,
        @NotBlank String startDate,
        @NotBlank String endDate,
        @PositiveOrZero Integer budget,
        String note,
        List<@Valid PlanRouteItemRequest> routeItems
) {
    public TravelPlan toTravelPlan(String userId) {
        return TravelPlan.createOwned(
                id.strip(),
                userId,
                title.strip(),
                startDate.strip(),
                endDate.strip(),
                budget == null ? 0 : budget,
                trimToEmpty(note)
        );
    }

    public List<PlanItem> toPlanItems() {
        if (routeItems == null) {
            return List.of();
        }
        return routeItems.stream()
                .map(routeItem -> routeItem.toPlanItem(id.strip()))
                .toList();
    }

    private static String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.strip();
    }
}
