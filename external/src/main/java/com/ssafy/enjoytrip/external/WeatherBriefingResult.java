package com.ssafy.enjoytrip.external;

public record WeatherBriefingResult(
        String region,
        String condition,
        Integer temperature,
        Integer rainChance,
        String sunrise,
        String sunset,
        Integer tempMin,
        Integer tempMax
) {
}
