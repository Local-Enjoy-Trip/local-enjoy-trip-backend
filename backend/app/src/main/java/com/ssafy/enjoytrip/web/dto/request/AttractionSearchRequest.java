package com.ssafy.enjoytrip.web.dto.request;

public record AttractionSearchRequest(
    String mapX,
    String mapY,
    String radius,
    String contentTypeId,
    String keyword,
    String sidoCode,
    String gugunCode
) {}
