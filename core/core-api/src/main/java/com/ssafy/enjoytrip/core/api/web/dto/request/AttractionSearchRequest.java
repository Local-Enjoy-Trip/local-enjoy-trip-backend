package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.query.AttractionSearchCondition;

public record AttractionSearchRequest(
    String mapX,
    String mapY,
    String radius,
    String contentTypeId,
    String keyword,
    String sidoCode,
    String gugunCode
) {
    public AttractionSearchCondition toCondition() {
        return new AttractionSearchCondition(
                trimToEmpty(sidoCode),
                trimToEmpty(gugunCode),
                trimToEmpty(contentTypeId),
                trimToEmpty(keyword),
                trimToEmpty(mapX),
                trimToEmpty(mapY),
                trimToEmpty(radius)
        );
    }

    private static String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
