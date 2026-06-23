package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.PlanRouteItem;
import com.ssafy.enjoytrip.core.domain.TravelPlan;
import java.util.List;

public record PlanResponse(
        String id,
        String title,
        String startDate,
        String endDate,
        int budget,
        String note,
        List<PlanRouteItemResponse> routeItems,
        String createdAt
) {
    public static PlanResponse from(TravelPlan plan, List<PlanRouteItem> routeItems) {
        return new PlanResponse(
                plan.id(),
                plan.title(),
                plan.startDate(),
                plan.endDate(),
                plan.budget(),
                value(plan.note()),
                toRouteItemResponses(routeItems),
                value(plan.createdAt())
        );
    }

    private static List<PlanRouteItemResponse> toRouteItemResponses(List<PlanRouteItem> routeItems) {
        if (routeItems == null || routeItems.isEmpty()) {
            return List.of();
        }

        return routeItems.stream()
                .map(PlanRouteItemResponse::from)
                .toList();
    }

    private static String value(String value) {
        if (value == null) {
            return "";
        }

        return value;
    }
}
