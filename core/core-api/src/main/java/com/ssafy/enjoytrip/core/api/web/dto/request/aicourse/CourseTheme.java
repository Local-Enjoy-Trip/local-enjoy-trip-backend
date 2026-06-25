package com.ssafy.enjoytrip.core.api.web.dto.request.aicourse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseTheme {
    FOOD("동네 맛집"),
    CAFE("감성 카페"),
    WALK("로컬 산책"),
    CULTURE("문화 혹은 전시"),
    NATURE("자연 속 휴식"),
    PHOTO("사진 명소"),
    MARKET("시장 골목"),
    SHOPPING("쇼핑");

    private final String label;
}
