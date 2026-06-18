package com.ssafy.enjoytrip.core.domain;

public record WeatherSummary(
        String region,
        String condition,
        Integer temperature,
        Integer rainChance,
        String sunrise,
        String sunset
) {
    public WeatherSummary withFallback(WeatherSummary fallback) {
        return new WeatherSummary(
                notBlank(region) ? region : fallback.region(),
                notBlank(condition) ? condition : fallback.condition(),
                temperature != null ? temperature : fallback.temperature(),
                rainChance != null ? rainChance : fallback.rainChance(),
                notBlank(sunrise) ? sunrise : fallback.sunrise(),
                notBlank(sunset) ? sunset : fallback.sunset()
        );
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
