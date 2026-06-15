package com.ssafy.enjoytrip.web.dto.request;

import com.ssafy.enjoytrip.service.command.PlanMutationCommand;
import com.ssafy.enjoytrip.service.command.PlanRouteItemCommand;
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
    public PlanMutationCommand toCommand() {
        return new PlanMutationCommand(
                id.strip(),
                title.strip(),
                startDate.strip(),
                endDate.strip(),
                budget == null ? 0 : budget,
                trimToEmpty(note),
                toRouteItemCommands(routeItems)
        );
    }

    private static List<PlanRouteItemCommand> toRouteItemCommands(List<PlanRouteItemRequest> routeItems) {
        if (routeItems == null) {
            return List.of();
        }
        return routeItems.stream().map(PlanRouteItemRequest::toCommand).toList();
    }

    private static String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.strip();
    }
}
