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
    private static final String ONE_CALL_URL = "https://api.openweathermap.org/data/3.0/onecall";
    private static final int HOURLY_FORECAST_LIMIT = 6;
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

    private static URI oneCallUri(String apiKey, double latitude, double longitude) {
        return URI.create(ONE_CALL_URL + "?lat=" + latitude
                + "&lon=" + longitude
                + "&appid=" + urlEncode(apiKey)
                + "&units=metric"
                + "&lang=kr"
                + "&exclude=minutely,alerts");
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

    public WeatherBriefingWithForecast findWeatherWithForecast(double latitude,
                                                               double longitude,
                                                               String regionName) {
        if (!notBlank(apiKey)) {
            throw new IllegalStateException(
                    "OpenWeatherMap API 키가 없습니다. enjoytrip.external.open-weather-map.api-key, "
                            + "OPENWEATHERMAP_API_KEY 또는 OPENWEATHER_API_KEY를 설정하세요."
            );
        }

        String oneCallBody = fetch(oneCallUri(apiKey, latitude, longitude));

        return parseWeatherWithForecast(regionName, oneCallBody);
    }

    private WeatherBriefingWithForecast parseWeatherWithForecast(String regionName, String oneCallBody) {
        try {
            JsonNode oneCallJson = objectMapper.readTree(oneCallBody);
            JsonNode currentJson = requiredObject(oneCallJson, "current");
            JsonNode hourlyNode = requiredArray(oneCallJson, "hourly");

            List<WeatherForecastResult> forecasts = hourlyForecasts(hourlyNode);
            if (forecasts.size() < HOURLY_FORECAST_LIMIT) {
                throw new IllegalArgumentException("OpenWeatherMap 시간별 예보가 부족합니다");
            }

            Integer currentTemp = roundedInteger(currentJson, "temp");
            WeatherBriefingResult current = new WeatherBriefingResult(
                    regionName,
                    weatherCondition(currentJson),
                    currentTemp,
                    rainChance(hourlyNode.get(0)),
                    epochSecondsToKoreanTime(numberValue(currentJson, "sunrise")),
                    epochSecondsToKoreanTime(numberValue(currentJson, "sunset")),
                    firstDailyTemperature(oneCallJson, "min", currentTemp, forecasts),
                    firstDailyTemperature(oneCallJson, "max", currentTemp, forecasts)
            );

            return new WeatherBriefingWithForecast(current, forecasts);
        } catch (Exception ex) {
            throw new IllegalStateException("OpenWeatherMap API 응답을 파싱하지 못했습니다", ex);
        }
    }

    private static JsonNode requiredObject(JsonNode parent, String fieldName) {
        JsonNode node = parent.path(fieldName);
        if (!node.isObject()) {
            throw new IllegalArgumentException(
                    "OpenWeatherMap 필드가 누락되었습니다: " + fieldName
            );
        }
        return node;
    }

    private static JsonNode requiredArray(JsonNode parent, String fieldName) {
        JsonNode node = parent.path(fieldName);
        if (!node.isArray()) {
            throw new IllegalArgumentException(
                    "OpenWeatherMap 필드가 누락되었습니다: " + fieldName
            );
        }
        return node;
    }

    private static List<WeatherForecastResult> hourlyForecasts(JsonNode hourlyNode) {
        List<WeatherForecastResult> forecasts = new ArrayList<>();
        int startIndex = hourlyNode.size() > HOURLY_FORECAST_LIMIT ? 1 : 0;

        for (int i = startIndex; i < hourlyNode.size() && forecasts.size() < HOURLY_FORECAST_LIMIT; i++) {
            JsonNode item = hourlyNode.get(i);
            forecasts.add(new WeatherForecastResult(
                    epochSecondsToKoreanTime(numberValue(item, "dt")),
                    roundedInteger(item, "temp"),
                    weatherCondition(item),
                    rainChance(item)
            ));
        }

        return forecasts;
    }

    private static String weatherCondition(JsonNode node) {
        JsonNode weatherNode = node.path("weather");
        if (!weatherNode.isArray() || weatherNode.isEmpty()) {
            return "맑음";
        }

        JsonNode firstWeather = weatherNode.get(0);
        String description = firstWeather.path("description").asText();
        if (notBlank(description)) {
            return description;
        }

        String condition = conditionFromMain(firstWeather.path("main").asText());
        return notBlank(condition) ? condition : "맑음";
    }

    private static Integer rainChance(JsonNode node) {
        if (!node.hasNonNull("pop")) {
            return null;
        }
        return clamp((int) Math.round(node.path("pop").asDouble() * 100), 0, 100);
    }

    private static Integer roundedInteger(JsonNode node, String fieldName) {
        if (!node.hasNonNull(fieldName)) {
            return null;
        }
        return roundedInteger(node.path(fieldName).asDouble());
    }

    private static Double numberValue(JsonNode node, String fieldName) {
        if (!node.hasNonNull(fieldName)) {
            return null;
        }
        return node.path(fieldName).asDouble();
    }

    private static Integer firstDailyTemperature(JsonNode oneCallJson,
                                                 String fieldName,
                                                 Integer currentTemp,
                                                 List<WeatherForecastResult> forecasts) {
        JsonNode dailyNode = oneCallJson.path("daily");
        if (dailyNode.isArray() && !dailyNode.isEmpty()) {
            Integer dailyTemperature = roundedInteger(dailyNode.get(0).path("temp"), fieldName);
            if (dailyTemperature != null) {
                return dailyTemperature;
            }
        }

        return aggregateTemperature(fieldName, currentTemp, forecasts);
    }

    private static Integer aggregateTemperature(String fieldName,
                                                Integer currentTemp,
                                                List<WeatherForecastResult> forecasts) {
        List<Integer> temperatures = new ArrayList<>();
        if (currentTemp != null) {
            temperatures.add(currentTemp);
        }
        forecasts.stream()
                .map(WeatherForecastResult::temperature)
                .filter(temperature -> temperature != null)
                .forEach(temperatures::add);

        if (temperatures.isEmpty()) {
            return null;
        }
        return "min".equals(fieldName)
                ? temperatures.stream().min(Integer::compareTo).orElse(null)
                : temperatures.stream().max(Integer::compareTo).orElse(null);
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
