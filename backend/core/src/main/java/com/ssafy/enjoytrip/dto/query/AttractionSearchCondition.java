package com.ssafy.enjoytrip.dto.query;

public record AttractionSearchCondition(
        String sidoCode,
        String gugunCode,
        String contentTypeId,
        String keyword,
        String mapX,
        String mapY,
        String radius
) {
}
