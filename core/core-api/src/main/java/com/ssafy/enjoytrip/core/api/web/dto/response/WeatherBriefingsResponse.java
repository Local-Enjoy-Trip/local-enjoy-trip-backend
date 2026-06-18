package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.WeatherSummary;

import java.util.List;

public record WeatherBriefingsResponse(List<WeatherSummary> weather) {
}
