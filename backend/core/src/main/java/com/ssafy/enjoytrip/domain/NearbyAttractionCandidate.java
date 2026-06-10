package com.ssafy.enjoytrip.domain;

public record NearbyAttractionCandidate(
        Attraction attraction,
        double distanceMeters
) {
}
