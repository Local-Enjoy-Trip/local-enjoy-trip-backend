package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.WeatherForecast;
import com.ssafy.enjoytrip.core.domain.WeatherBriefingWithForecastDomain;
import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import com.ssafy.enjoytrip.external.OpenWeatherMapWeatherClient;
import com.ssafy.enjoytrip.external.WeatherBriefingWithForecast;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int HOURLY_FORECAST_LIMIT = 6;
    private static final List<WeatherSummary> FALLBACK_BRIEFINGS = List.of(
            new WeatherSummary("서울", "맑음", 22, 10, "05:23", "19:33", 15, 25),
            new WeatherSummary("부산", "구름 조금", 21, 20, "05:17", "19:22", 16, 24),
            new WeatherSummary("제주", "바람 강함", 23, 30, "05:35", "19:25", 18, 26)
    );

    private final OpenWeatherMapWeatherClient weatherClient;

    public List<WeatherSummary> findWeatherBriefings() {
        try {
            return weatherClient.findWeatherBriefings().stream()
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
                    .toList();
        } catch (Exception e) {
            log.error("날씨 브리핑 호출 에러 발생 : ", e);
            return FALLBACK_BRIEFINGS;
        }
    }

    public WeatherBriefingWithForecastDomain findWeatherWithForecast(Double latitude,
                                                                     Double longitude,
                                                                     String regionName) {
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

            WeatherBriefingWithForecast clientResult = weatherClient.findWeatherWithForecast(
                    lat,
                    lon,
                    regionName
            );

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
            log.error("날씨 호출 에러 발생 : " , e);
            WeatherSummary fallbackWeather = new WeatherSummary(
                    regionName,
                    "맑음",
                    22,
                    10,
                    "05:23",
                    "19:33",
                    15,
                    25
            );
            return new WeatherBriefingWithForecastDomain(fallbackWeather, fallbackForecasts());
        }
    }

    private static List<WeatherForecast> fallbackForecasts() {
        LocalTime currentHour = LocalTime.now(KOREA).truncatedTo(ChronoUnit.HOURS);
        List<WeatherForecast> forecasts = new ArrayList<>();

        for (int hour = 1; hour <= HOURLY_FORECAST_LIMIT; hour++) {
            forecasts.add(new WeatherForecast(
                    currentHour.plusHours(hour).format(HOUR_FORMAT),
                    22,
                    "맑음",
                    10
            ));
        }

        return forecasts;
    }

}
