package com.ssafy.enjoytrip.core.domain.vo;

import java.io.Serializable;

public record RatingStats(
        double ratingAverage,
        int ratingCount
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
