package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public record NeighborhoodBriefingPrompt(
        String region,
        WeatherSummary weather,
        List<CourseBriefingCandidate> courseCandidates
) {
    public NeighborhoodBriefingPrompt {
        courseCandidates = courseCandidates == null ? List.of() : List.copyOf(courseCandidates);
    }
}
