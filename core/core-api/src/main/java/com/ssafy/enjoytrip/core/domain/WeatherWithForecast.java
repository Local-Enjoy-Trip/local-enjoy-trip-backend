package com.ssafy.enjoytrip.core.domain;

import java.io.Serializable;
import java.util.List;

public record WeatherWithForecast(
        WeatherSummary current,
        List<WeatherForecast> forecasts,
        boolean isFallback
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public WeatherWithForecast(WeatherSummary current, List<WeatherForecast> forecasts) {
        this(current, forecasts, false);
    }
}
