package com.ssafy.enjoytrip.external.briefing;

import com.ssafy.enjoytrip.external.WeatherBriefingResult;
import java.util.List;

public record NeighborhoodBriefingPromptData(
        String region,
        WeatherBriefingResult weather,
        List<CourseBriefingCandidateData> courseCandidates
) {
    public NeighborhoodBriefingPromptData {
        courseCandidates = courseCandidates == null ? List.of() : List.copyOf(courseCandidates);
    }
}
