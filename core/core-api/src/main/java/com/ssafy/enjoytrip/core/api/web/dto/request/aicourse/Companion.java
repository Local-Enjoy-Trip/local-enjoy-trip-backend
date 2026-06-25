package com.ssafy.enjoytrip.core.api.web.dto.request.aicourse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Companion {
    ALONE("혼자"),
    WITH_FRIEND("친구와"),
    WITH_PARTNER("연인과"),
    WITH_CHILD("아이와"),
    WITH_PARENTS("부모님과"),
    WITH_PET("반려동물과");

    private final String label;
}
