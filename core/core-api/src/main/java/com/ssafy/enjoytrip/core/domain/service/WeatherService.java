package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.WeatherForecast;
import com.ssafy.enjoytrip.core.domain.WeatherWithForecast;
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
    private final OpenWeatherMapWeatherClient weatherClient;

    public WeatherWithForecast findWeatherWithForecast(Double latitude,
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

            return new WeatherWithForecast(weather, forecasts);
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
            return new WeatherWithForecast(fallbackWeather, fallbackForecasts());
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
