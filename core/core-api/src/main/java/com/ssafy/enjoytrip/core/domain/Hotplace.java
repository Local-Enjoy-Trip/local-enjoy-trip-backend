package com.ssafy.enjoytrip.core.domain;

public record Hotplace(
        String id,
        Long memberId,
        String title,
        String type,
        String visitDate,
        Double lat,
        Double lng,
        String description,
        String photo,
        String createdAt
) {
}
