package com.ssafy.enjoytrip.web.dto.request;

public record PlanRequest(
    String action,
    String id,
    String userId,
    String title,
    String startDate,
    String endDate,
    String budget,
    String note,
    String routeItems
) {}
