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

}
