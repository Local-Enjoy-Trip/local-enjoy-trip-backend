package com.ssafy.enjoytrip.domain;

public record AttractionSearchCondition(
        String sidoCode,
        String gugunCode,
        String contentTypeId,
        String keyword,
        String mapX,
        String mapY,
        String radius
) {
    public AttractionSearchCondition {
        sidoCode = normalize(sidoCode);
        gugunCode = normalize(gugunCode);
        contentTypeId = normalize(contentTypeId);
        keyword = normalize(keyword);
        mapX = normalize(mapX);
        mapY = normalize(mapY);
        radius = normalize(radius);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
