package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.PlanItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlanRouteItemRequest(
        @NotNull @Positive Long attractionId,
        @Min(1) Integer day,
        String memo,
        @Min(1) Integer stayMinutes
) {
    public PlanItem toPlanItem(String planId) {
        return new PlanItem(
                null,
                planId,
                attractionId,
                0,
                day == null ? 1 : day,
                trimToEmpty(memo),
                stayMinutes == null ? 90 : stayMinutes
        );
    }

    private static String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.strip();
    }
}
