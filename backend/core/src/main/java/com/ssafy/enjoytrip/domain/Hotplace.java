package com.ssafy.enjoytrip.domain;

public record Hotplace(
        String id,
        String userId,
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
