package com.ssafy.enjoytrip.external;

public record WeatherForecastResult(
        String time,
        Integer temperature,
        String condition,
        Integer rainChance
) {
}
