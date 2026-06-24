package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.domain.vo.TemperatureRange;
import java.io.Serializable;

public record WeatherSummary(
        String region,
        String condition,
        Integer temperature,
        Integer rainChance,
        String sunrise,
        String sunset,
        TemperatureRange temperatureRange
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
