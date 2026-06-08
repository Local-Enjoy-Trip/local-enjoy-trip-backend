package com.ssafy.enjoytrip.domain;

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
