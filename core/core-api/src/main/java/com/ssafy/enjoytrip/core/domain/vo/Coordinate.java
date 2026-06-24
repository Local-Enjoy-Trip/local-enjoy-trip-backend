package com.ssafy.enjoytrip.core.domain.vo;

import java.io.Serializable;

public record Coordinate(double latitude, double longitude) implements Serializable {
    private static final long serialVersionUID = 1L;
}
