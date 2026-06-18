package com.ssafy.enjoytrip.core.domain;

public record PlanItem(
        Long id,
        String planId,
        Long attractionId,
        int position,
        int day,
        String memo,
        int stayMinutes
) {
}
