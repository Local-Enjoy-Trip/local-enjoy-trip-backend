package com.ssafy.enjoytrip.web.dto.response;

public record PlanResponse(
        String id,
        String userId,
        String title,
        String startDate,
        String endDate,
        int budget,
        String note,
        Object routeItems,
        String createdAt
) {
}
