package com.ssafy.enjoytrip.web.dto.request;

import com.ssafy.enjoytrip.service.command.PlanRouteItemCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlanRouteItemRequest(
        @NotNull @Positive Long attractionId,
        @Min(1) Integer day,
        String memo,
        @Min(1) Integer stayMinutes
) {
    public PlanRouteItemCommand toCommand() {
        return new PlanRouteItemCommand(attractionId, day, memo, stayMinutes);
    }
}
