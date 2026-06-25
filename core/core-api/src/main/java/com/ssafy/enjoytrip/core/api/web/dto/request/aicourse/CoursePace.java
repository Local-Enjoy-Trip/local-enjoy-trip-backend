package com.ssafy.enjoytrip.core.api.web.dto.request.aicourse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoursePace {
    RELAXED("여유롭게", 3),
    MODERATE("알맞게", 4),
    PACKED("알차게", 5);

    private final String label;
    private final int placeCount;
}
