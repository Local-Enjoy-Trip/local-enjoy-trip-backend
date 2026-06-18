package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.PlanItem;
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
    public String normalizedTitle() {
        return trimToNull(title);
    }

    public String normalizedStartDate() {
        return trimToNull(startDate);
    }

    public String normalizedEndDate() {
        return trimToNull(endDate);
    }

    public String normalizedNote() {
        return trimToNull(note);
    }

    public List<PlanItem> toPlanItems(String planId) {
        if (routeItems == null) {
            return null;
        }

        return routeItems.stream()
                .map(routeItem -> routeItem.toPlanItem(planId))
                .toList();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}
