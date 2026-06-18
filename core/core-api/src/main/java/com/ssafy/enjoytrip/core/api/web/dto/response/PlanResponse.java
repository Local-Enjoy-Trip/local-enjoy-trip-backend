package com.ssafy.enjoytrip.core.api.web.dto.response;

import java.util.List;

public record PlanResponse(
        String id,
        String userId,
        String title,
        String startDate,
        String endDate,
        int budget,
        String note,
        List<PlanRouteItemResponse> routeItems,
        String createdAt
) {
}
