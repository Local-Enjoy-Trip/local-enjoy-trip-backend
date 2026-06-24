package com.ssafy.enjoytrip.core.domain.vo;

import java.io.Serializable;

public record TemperatureRange(
        Integer tempMin,
        Integer tempMax
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
