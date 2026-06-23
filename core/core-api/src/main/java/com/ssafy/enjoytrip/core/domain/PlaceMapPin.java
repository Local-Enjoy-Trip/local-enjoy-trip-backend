package com.ssafy.enjoytrip.core.domain;

public record PlaceMapPin(
        Long id,
        String title,
        String address,
        Double latitude,
        Double longitude,
        String imageUrl,
        String contentTypeId,
        double distanceMeters,
        boolean saved,
        int saveCount,
        double ratingAverage,
        int ratingCount
) {
}
