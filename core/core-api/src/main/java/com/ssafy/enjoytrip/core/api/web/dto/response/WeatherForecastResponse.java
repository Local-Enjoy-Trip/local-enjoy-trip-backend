package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.WeatherForecast;

public record WeatherForecastResponse(
        String time,
        Integer temperature,
        String condition,
        Integer rainChance
) {
    public WeatherForecastResponse(WeatherForecast forecast) {
        this(
                forecast.time(),
                forecast.temperature(),
                forecast.condition(),
                forecast.rainChance()
        );
    }
}
