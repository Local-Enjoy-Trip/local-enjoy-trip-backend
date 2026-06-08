package com.ssafy.enjoytrip.domain;

public record TravelPlan(
        String id,
        String userId,
        String title,
        String startDate,
        String endDate,
        int budget,
        String note,
        String routeItemsJson,
        String createdAt
) {
}
