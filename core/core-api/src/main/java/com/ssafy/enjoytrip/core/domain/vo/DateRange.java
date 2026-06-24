package com.ssafy.enjoytrip.core.domain.vo;

import java.io.Serializable;

public record DateRange(
        String startDate,
        String endDate
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
