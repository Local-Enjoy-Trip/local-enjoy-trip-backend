package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.PlaceMapPin;

public record PlaceMapPinResponse(
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
    public static PlaceMapPinResponse from(PlaceMapPin pin) {
        return new PlaceMapPinResponse(
                pin.id(),
                pin.title(),
                pin.address(),
                pin.latitude(),
                pin.longitude(),
                pin.imageUrl(),
                pin.contentTypeId(),
                pin.distanceMeters(),
                pin.favorited(),
                pin.ratingAverage(),
                pin.ratingCount()
        );
    }
}
