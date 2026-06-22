package com.ssafy.enjoytrip.external;

import java.util.List;

public record WeatherBriefingWithForecast(
        WeatherBriefingResult current,
        List<WeatherForecastResult> forecasts
) {
}
