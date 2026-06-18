package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.MapCenter;

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
