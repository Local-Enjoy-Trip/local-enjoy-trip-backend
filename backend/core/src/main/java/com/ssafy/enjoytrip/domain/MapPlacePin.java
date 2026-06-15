package com.ssafy.enjoytrip.domain;

public record MapPlacePin(
        Long id,
        String title,
        String address,
        Double latitude,
        Double longitude,
        String imageUrl,
        double distanceMeters
) {
    public static MapPlacePin from(NearbyAttractionCandidate candidate) {
        Attraction attraction = candidate.attraction();

        return new MapPlacePin(
                attraction.id(),
                attraction.title(),
                attraction.addr1(),
                attraction.latitude(),
                attraction.longitude(),
                firstNonBlank(attraction.firstImage(), attraction.firstImage2()),
                candidate.distanceMeters()
        );
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        if (second != null && !second.isBlank()) {
            return second;
        }

        return null;
    }
}
