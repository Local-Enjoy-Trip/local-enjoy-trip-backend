package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.WeatherForecast;
import com.ssafy.enjoytrip.core.domain.WeatherBriefingWithForecastDomain;
import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import com.ssafy.enjoytrip.external.OpenWeatherMapWeatherClient;
import com.ssafy.enjoytrip.external.WeatherBriefingWithForecast;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private static final List<WeatherSummary> FALLBACK_BRIEFINGS = List.of(
            new WeatherSummary("서울", "맑음", 22, 10, "05:23", "19:33", 15, 25),
            new WeatherSummary("부산", "구름 조금", 21, 20, "05:17", "19:22", 16, 24),
            new WeatherSummary("제주", "바람 강함", 23, 30, "05:35", "19:25", 18, 26)
    );

    private final OpenWeatherMapWeatherClient weatherClient;

    public List<WeatherSummary> findWeatherBriefings() {
        return completeWithFallback(weatherClient.findWeatherBriefings().stream()
                .map(briefing -> new WeatherSummary(
                        briefing.region(),
                        briefing.condition(),
                        briefing.temperature(),
                        briefing.rainChance(),
                        briefing.sunrise(),
                        briefing.sunset(),
                        briefing.tempMin(),
                        briefing.tempMax()
                ))
                .toList());
    }

    public WeatherBriefingWithForecastDomain findWeatherWithForecast(Double latitude, Double longitude, String regionName) {
        try {
            double lat = latitude != null ? latitude : 37.5665;
            double lon = longitude != null ? longitude : 126.9780;

            if (latitude == null || longitude == null) {
                if (regionName.contains("부산")) {
                    lat = 35.1796;
                    lon = 129.0756;
                } else if (regionName.contains("제주")) {
                    lat = 33.4996;
                    lon = 126.5312;
                }
            }

            WeatherBriefingWithForecast clientResult = weatherClient.findWeatherWithForecast(lat, lon, regionName);

            WeatherSummary weather = new WeatherSummary(
                    clientResult.current().region(),
                    clientResult.current().condition(),
                    clientResult.current().temperature(),
                    clientResult.current().rainChance(),
                    clientResult.current().sunrise(),
                    clientResult.current().sunset(),
                    clientResult.current().tempMin(),
                    clientResult.current().tempMax()
            );

            List<WeatherForecast> forecasts = clientResult.forecasts().stream()
                    .map(f -> new WeatherForecast(
                            f.time(),
                            f.temperature(),
                            f.condition(),
                            f.rainChance()
                    ))
                    .toList();

            return new WeatherBriefingWithForecastDomain(weather, forecasts);
        } catch (Exception e) {
            WeatherSummary fallbackWeather = new WeatherSummary(regionName, "맑음", 22, 10, "05:23", "19:33", 15, 25);
            List<WeatherForecast> fallbackForecasts = List.of(
                    new WeatherForecast("12:00", 22, "맑음", 10),
                    new WeatherForecast("15:00", 24, "맑음", 10)
            );
            return new WeatherBriefingWithForecastDomain(fallbackWeather, fallbackForecasts);
        }
    }

    private List<WeatherSummary> completeWithFallback(List<WeatherSummary> liveBriefings) {
        if (liveBriefings == null || liveBriefings.isEmpty()) {
            return FALLBACK_BRIEFINGS;
        }

        Map<String, WeatherSummary> liveByRegion = new LinkedHashMap<>();
        for (WeatherSummary briefing : liveBriefings) {
            if (briefing != null && briefing.region() != null && !briefing.region().isBlank()) {
                liveByRegion.putIfAbsent(briefing.region(), briefing);
            }
        }

        return FALLBACK_BRIEFINGS.stream()
                .map(fallback -> liveByRegion
                        .getOrDefault(fallback.region(), fallback)
                        .withFallback(fallback))
                .toList();
    }
}

