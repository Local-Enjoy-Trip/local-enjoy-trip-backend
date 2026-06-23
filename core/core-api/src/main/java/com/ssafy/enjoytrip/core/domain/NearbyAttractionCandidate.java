package com.ssafy.enjoytrip.core.domain;


public record NearbyAttractionCandidate(
        Attraction attraction,
        double distanceMeters
) {
}
