package com.ssafy.enjoytrip.web.dto.request;

public record HotplaceRequest(
    String action,
    String id,
    String userId,
    String title,
    String type,
    String visitDate,
    String lat,
    String lng,
    String description,
    String photo
) {}
