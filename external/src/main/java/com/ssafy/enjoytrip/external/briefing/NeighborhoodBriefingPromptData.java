package com.ssafy.enjoytrip.external.briefing;

import com.ssafy.enjoytrip.external.WeatherBriefingResult;
import java.util.List;

public record NeighborhoodBriefingPromptData(
        String region,
        WeatherBriefingResult weather,
        List<LocalPlaceData> localPlaces
) {
    public NeighborhoodBriefingPromptData {
        localPlaces = localPlaces == null ? List.of() : List.copyOf(localPlaces);
    }
}
