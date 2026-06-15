package com.ssafy.enjoytrip.domain;

public record PlaceMapPin(
        Long id,
        String title,
        String address,
        Double latitude,
        Double longitude,
        String imageUrl,
        String contentTypeId,
        double distanceMeters,
        boolean favorited,
        double ratingAverage,
        int ratingCount
) {
}
