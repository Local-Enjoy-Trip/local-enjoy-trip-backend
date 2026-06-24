package com.ssafy.enjoytrip.core.domain;

import java.io.Serializable;
import java.util.List;

public record NeighborhoodBriefing(
        String region,
        String briefing,
        WeatherSummary weather,
        List<WeatherForecast> forecasts,
        boolean isFallback
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public NeighborhoodBriefing(String region, String briefing, WeatherSummary weather, List<WeatherForecast> forecasts) {
        this(region, briefing, weather, forecasts, false);
    }
}

