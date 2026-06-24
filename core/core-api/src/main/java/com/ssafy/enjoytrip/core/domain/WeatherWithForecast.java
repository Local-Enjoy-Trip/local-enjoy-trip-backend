package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public record WeatherWithForecast(
        WeatherSummary current,
        List<WeatherForecast> forecasts
) {
}
