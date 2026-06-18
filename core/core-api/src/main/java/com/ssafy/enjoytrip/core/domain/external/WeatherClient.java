package com.ssafy.enjoytrip.core.domain.external;

import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import java.util.List;

public interface WeatherClient {
    List<WeatherSummary> findWeatherBriefings();
}
