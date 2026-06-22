package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.WeatherSummary;

public record WeatherSummaryResponse(
        String region,
        String condition,
        Integer temperature,
        Integer rainChance,
        String sunrise,
        String sunset,
        Integer tempMin,
        Integer tempMax
) {
    public WeatherSummaryResponse(WeatherSummary weather) {
        this(
                weather.region(),
                weather.condition(),
                weather.temperature(),
                weather.rainChance(),
                weather.sunrise(),
                weather.sunset(),
                weather.tempMin(),
                weather.tempMax()
        );
    }
}
