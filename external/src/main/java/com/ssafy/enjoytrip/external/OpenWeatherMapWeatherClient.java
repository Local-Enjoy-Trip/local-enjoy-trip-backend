package com.ssafy.enjoytrip.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OpenWeatherMapWeatherClient {
    private static final String CURRENT_WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm").withZone(KOREA);
    private static final List<RegionCoordinate> DEFAULT_REGIONS = List.of(
            new RegionCoordinate("서울", 37.5665, 126.9780),
            new RegionCoordinate("부산", 35.1796, 129.0756),
            new RegionCoordinate("제주", 33.4996, 126.5312)
    );

    private final RestClient restClient;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenWeatherMapWeatherClient(
            RestClient restClient,
            @Value("${enjoytrip.external.open-weather-map.api-key:}") String apiKey
    ) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public List<WeatherBriefingResult> findWeatherBriefings() {
        if (!notBlank(apiKey)) {
            throw new IllegalStateException(
                    "OpenWeatherMap API 키가 없습니다. enjoytrip.external.open-weather-map.api-key, "
                            + "OPENWEATHERMAP_API_KEY 또는 OPENWEATHER_API_KEY를 설정하세요."
            );
        }

        List<WeatherBriefingResult> rows = new ArrayList<>();
        for (RegionCoordinate region : DEFAULT_REGIONS) {
            String currentBody = fetch(currentWeatherUri(apiKey, region));
            String forecastBody = fetch(forecastUri(apiKey, region));
            WeatherBriefingResult summary = toWeatherSummary(region.name(), currentBody, forecastBody);
            if (hasLiveValue(summary)) {
                rows.add(summary);
            }
        }
        return rows;
    }

    private String fetch(URI uri) {
        try {
            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw new IllegalStateException("OpenWeatherMap API 호출에 실패했습니다", ex);
        }
    }

    private static URI currentWeatherUri(String apiKey, RegionCoordinate region) {
        return URI.create(CURRENT_WEATHER_URL + baseQuery(apiKey, region));
    }

    private static URI forecastUri(String apiKey, RegionCoordinate region) {
        return URI.create(FORECAST_URL + baseQuery(apiKey, region) + "&cnt=1");
    }

    private static String baseQuery(String apiKey, RegionCoordinate region) {
        return "?lat=" + region.lat()
                + "&lon=" + region.lon()
                + "&appid=" + urlEncode(apiKey)
                + "&units=metric"
                + "&lang=kr";
    }

    private WeatherBriefingResult toWeatherSummary(String region, String currentBody, String forecastBody) {
        try {
            requireJsonObject(currentBody, "weather");
            requireJsonObject(forecastBody, "list");
            return new WeatherBriefingResult(
                    region,
                    condition(currentBody, forecastBody),
                    roundedInteger(numberValue(currentBody, "temp")),
                    rainChance(forecastBody),
                    epochSecondsToKoreanTime(numberValue(currentBody, "sunrise")),
                    epochSecondsToKoreanTime(numberValue(currentBody, "sunset")),
                    roundedInteger(numberValue(currentBody, "temp_min")),
                    roundedInteger(numberValue(currentBody, "temp_max"))
            );
        } catch (Exception ex) {
            throw new IllegalStateException("OpenWeatherMap API 응답을 파싱하지 못했습니다", ex);
        }
    }

    private static String condition(String currentBody, String forecastBody) {
        String koreanDescription = stringValue(currentBody, "description");
        if (notBlank(koreanDescription)) {
            return koreanDescription;
        }
        return conditionFromMain(firstNotBlank(
                stringValue(currentBody, "main"),
                stringValue(forecastBody, "main")
        ));
    }

    private static Integer rainChance(String forecastBody) {
        Double pop = numberValue(forecastBody, "pop");
        if (pop != null) {
            return clamp((int) Math.round(pop * 100), 0, 100);
        }
        return null;
    }

    private static String epochSecondsToKoreanTime(Double value) {
        if (value == null) {
            return null;
        }
        return TIME_FORMAT.format(Instant.ofEpochSecond(value.longValue()));
    }

    private static boolean hasLiveValue(WeatherBriefingResult summary) {
        return notBlank(summary.condition())
                || summary.temperature() != null
                || summary.rainChance() != null
                || notBlank(summary.sunrise())
                || notBlank(summary.sunset());
    }

    private static String conditionFromMain(String main) {
        if (!notBlank(main)) {
            return null;
        }
        return switch (main) {
            case "Thunderstorm" -> "천둥번개";
            case "Drizzle" -> "이슬비";
            case "Rain" -> "비";
            case "Snow" -> "눈";
            case "Mist", "Smoke", "Haze", "Dust", "Fog", "Sand", "Ash", "Squall", "Tornado" -> "안개";
            case "Clear" -> "맑음";
            case "Clouds" -> "구름 많음";
            default -> main;
        };
    }

    private static void requireJsonObject(String body, String expectedField) {
        if (!notBlank(body)
                || !body.stripLeading().startsWith("{")
                || !body.contains("\"" + expectedField + "\"")) {
            throw new IllegalArgumentException(
                    "OpenWeatherMap 필드가 누락되었습니다: " + expectedField
            );
        }
    }

    private static Double numberValue(String body, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(body);
        if (!matcher.find()) {
            return null;
        }
        return Double.parseDouble(matcher.group(1));
    }

    private static String stringValue(String body, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"").matcher(body);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1).trim();
    }

    private static Integer roundedInteger(Double value) {
        if (value == null) {
            return null;
        }
        return (int) Math.round(value);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String firstNotBlank(String first, String second) {
        if (notBlank(first)) {
            return first;
        }
        return second;
    }

    public WeatherBriefingWithForecast findWeatherWithForecast(double latitude, double longitude, String regionName) {
        if (!notBlank(apiKey)) {
            throw new IllegalStateException(
                    "OpenWeatherMap API 키가 없습니다. enjoytrip.external.open-weather-map.api-key, "
                            + "OPENWEATHERMAP_API_KEY 또는 OPENWEATHER_API_KEY를 설정하세요."
            );
        }

        URI currentUri = URI.create(CURRENT_WEATHER_URL + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + urlEncode(apiKey) + "&units=metric&lang=kr");
        URI forecastUri = URI.create(FORECAST_URL + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + urlEncode(apiKey) + "&units=metric&lang=kr&cnt=2");

        String currentBody = fetch(currentUri);
        String forecastBody = fetch(forecastUri);

        return parseWeatherWithForecast(regionName, currentBody, forecastBody);
    }

    private WeatherBriefingWithForecast parseWeatherWithForecast(String regionName, String currentBody, String forecastBody) {
        try {
            JsonNode currentJson = objectMapper.readTree(currentBody);
            JsonNode forecastJson = objectMapper.readTree(forecastBody);

            String condition = "맑음";
            if (currentJson.has("weather") && currentJson.get("weather").isArray() && currentJson.get("weather").size() > 0) {
                JsonNode weatherNode = currentJson.get("weather").get(0);
                String desc = weatherNode.path("description").asText();
                if (notBlank(desc)) {
                    condition = desc;
                } else {
                    condition = conditionFromMain(weatherNode.path("main").asText());
                }
            }

            Integer temp = null;
            Integer tempMin = null;
            Integer tempMax = null;
            if (currentJson.has("main")) {
                temp = roundedInteger(currentJson.get("main").path("temp").asDouble());
                tempMin = roundedInteger(currentJson.get("main").path("temp_min").asDouble());
                tempMax = roundedInteger(currentJson.get("main").path("temp_max").asDouble());
            }

            String sunrise = null;
            String sunset = null;
            if (currentJson.has("sys")) {
                sunrise = epochSecondsToKoreanTime(currentJson.get("sys").path("sunrise").asDouble());
                sunset = epochSecondsToKoreanTime(currentJson.get("sys").path("sunset").asDouble());
            }

            Integer currentRainChance = null;
            JsonNode listNode = forecastJson.path("list");
            if (listNode.isArray() && listNode.size() > 0) {
                Double pop = listNode.get(0).path("pop").asDouble();
                currentRainChance = clamp((int) Math.round(pop * 100), 0, 100);
            }

            WeatherBriefingResult current = new WeatherBriefingResult(
                    regionName,
                    condition,
                    temp,
                    currentRainChance,
                    sunrise,
                    sunset,
                    tempMin,
                    tempMax
            );

            List<WeatherForecastResult> forecasts = new ArrayList<>();
            if (listNode.isArray()) {
                int limit = Math.min(2, listNode.size());
                for (int i = 0; i < limit; i++) {
                    JsonNode item = listNode.get(i);
                    long dt = item.path("dt").asLong();
                    String forecastTime = TIME_FORMAT.format(Instant.ofEpochSecond(dt));

                    Integer forecastTemp = null;
                    if (item.has("main")) {
                        forecastTemp = roundedInteger(item.get("main").path("temp").asDouble());
                    }

                    String forecastCondition = "맑음";
                    if (item.has("weather") && item.get("weather").isArray() && item.get("weather").size() > 0) {
                        JsonNode weatherNode = item.get("weather").get(0);
                        String desc = weatherNode.path("description").asText();
                        if (notBlank(desc)) {
                            forecastCondition = desc;
                        } else {
                            forecastCondition = conditionFromMain(weatherNode.path("main").asText());
                        }
                    }

                    Double popVal = item.path("pop").asDouble();
                    Integer forecastRainChance = clamp((int) Math.round(popVal * 100), 0, 100);

                    forecasts.add(new WeatherForecastResult(
                            forecastTime,
                            forecastTemp,
                            forecastCondition,
                            forecastRainChance
                    ));
                }
            }

            return new WeatherBriefingWithForecast(current, forecasts);
        } catch (Exception ex) {
            throw new IllegalStateException("OpenWeatherMap API 응답을 파싱하지 못했습니다", ex);
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private record RegionCoordinate(String name, double lat, double lon) {
    }
}
