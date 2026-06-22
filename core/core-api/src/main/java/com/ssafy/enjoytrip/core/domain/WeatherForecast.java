package com.ssafy.enjoytrip.core.domain;

import java.io.Serializable;

public record WeatherForecast(
        String time,
        Integer temperature,
        String condition,
        Integer rainChance
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
