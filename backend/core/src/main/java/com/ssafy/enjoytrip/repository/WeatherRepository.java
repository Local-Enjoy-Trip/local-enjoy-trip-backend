package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.WeatherSummary;

import java.util.List;

public interface WeatherRepository {
    List<WeatherSummary> findWeatherBriefings();
}
