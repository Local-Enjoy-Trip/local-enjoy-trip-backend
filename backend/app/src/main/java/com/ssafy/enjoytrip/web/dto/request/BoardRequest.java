package com.ssafy.enjoytrip.web.dto.request;

public record BoardRequest(
    String action,
    String id,
    String title,
    String content,
    String author
) {}
