package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.WeatherSummary;

import java.util.List;

public record WeatherBriefingsResponse(List<WeatherSummary> weather) {
}
