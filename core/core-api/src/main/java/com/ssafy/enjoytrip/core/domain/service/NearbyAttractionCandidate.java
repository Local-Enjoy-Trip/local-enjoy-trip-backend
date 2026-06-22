package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Attraction;

public record NearbyAttractionCandidate(
        Attraction attraction,
        double distanceMeters
) {
}
