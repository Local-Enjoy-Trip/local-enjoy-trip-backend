package com.ssafy.enjoytrip.core.domain;

import java.io.Serializable;

public record WeatherSummary(
        String region,
        String condition,
        Integer temperature,
        Integer rainChance,
        String sunrise,
        String sunset,
        Integer tempMin,
        Integer tempMax
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public WeatherSummary withFallback(WeatherSummary fallback) {
        return new WeatherSummary(
                notBlank(region) ? region : fallback.region(),
                notBlank(condition) ? condition : fallback.condition(),
                temperature != null ? temperature : fallback.temperature(),
                rainChance != null ? rainChance : fallback.rainChance(),
                notBlank(sunrise) ? sunrise : fallback.sunrise(),
                notBlank(sunset) ? sunset : fallback.sunset(),
                tempMin != null ? tempMin : fallback.tempMin(),
                tempMax != null ? tempMax : fallback.tempMax()
        );
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
