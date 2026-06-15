package com.ssafy.enjoytrip.domain;

public record MapCenter(
        double longitude,
        double latitude,
        String regionName,
        boolean fromRepresentativeLocation
) {
}
