package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.MapCenter;

public record MapCenterResponse(
        double longitude,
        double latitude,
        String regionName,
        boolean fromRepresentativeLocation
) {
    public static MapCenterResponse from(MapCenter center) {
        return new MapCenterResponse(
                center.longitude(),
                center.latitude(),
                center.regionName(),
                center.fromRepresentativeLocation()
        );
    }
}
