package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public record WeatherBriefingWithForecastDomain(
        WeatherSummary current,
        List<WeatherForecast> forecasts
) {
}
