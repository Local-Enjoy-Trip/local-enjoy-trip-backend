package com.ssafy.enjoytrip.web.dto.request;

public record ChargerSearchRequest(
    String zcode,
    String keyword,
    String pageNo,
    String numOfRows
) {}
