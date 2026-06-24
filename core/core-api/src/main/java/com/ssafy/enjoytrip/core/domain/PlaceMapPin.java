package com.ssafy.enjoytrip.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

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
        int ratingCount,
        int matchTier
) implements MapPin {
    @JsonProperty("type")
    @Override
    public String type() { return "PLACE"; }

    public PlaceMapPin(Long id, String title, String address, Double latitude, Double longitude, String imageUrl, String contentTypeId, double distanceMeters, boolean saved, int saveCount, double ratingAverage, int ratingCount) {
        this(id, title, address, latitude, longitude, imageUrl, contentTypeId, distanceMeters, saved, saveCount, ratingAverage, ratingCount, 0);
    }
}
