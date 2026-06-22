package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefing;
import java.util.List;

public record NeighborhoodBriefingResponse(
        String region,
        String briefing,
        WeatherSummaryResponse weather,
        List<WeatherForecastResponse> forecasts
) {
    public NeighborhoodBriefingResponse(NeighborhoodBriefing briefing) {
        this(
                briefing.region(),
                briefing.briefing(),
                briefing.weather() != null ? new WeatherSummaryResponse(briefing.weather()) : null,
                briefing.forecasts() != null ? briefing.forecasts().stream().map(WeatherForecastResponse::new).toList() : List.of()
        );
    }
}

