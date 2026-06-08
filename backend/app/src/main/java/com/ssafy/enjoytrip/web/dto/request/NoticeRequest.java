package com.ssafy.enjoytrip.web.dto.request;

public record NoticeRequest(
    String action,
    String id,
    String title,
    String content,
    String author
) {}
