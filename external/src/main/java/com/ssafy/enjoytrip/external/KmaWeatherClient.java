package com.ssafy.enjoytrip.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class KmaWeatherClient {
    private static final String NCST_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
    private static final String FCST_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
    private static final int HOURLY_FORECAST_LIMIT = 6;
    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

    private final RestClient restClient;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KmaWeatherClient(
            RestClient restClient,
            @Value("${enjoytrip.external.kma.api-key:}") String apiKey
    ) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public WeatherBriefingWithForecast findWeatherWithForecast(double latitude,
                                                               double longitude,
                                                               String regionName) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("기상청 API 키가 없습니다. KMA_API_KEY를 설정하세요.");
        }

        // 1. 위경도 -> 기상청 격자 좌표(nx, ny) 변환
        GridPoint gp = convertToGrid(latitude, longitude);

        // 2. 현재 시간에 따른 발표 날짜/시간 계산
        LocalDateTime now = LocalDateTime.now(KOREA);
        KmaTime ncstTime = calculateNcstTime(now);
        KmaTime fcstTime = calculateFcstTime(now);

        // 3. API 호출
        String ncstBody = fetch(NCST_URL, ncstTime.baseDate, ncstTime.baseTime, gp.nx, gp.ny);
        String fcstBody = fetch(FCST_URL, fcstTime.baseDate, fcstTime.baseTime, gp.nx, gp.ny);

        // 4. 데이터 파싱 및 조합
        return parseWeatherData(regionName, ncstBody, fcstBody);
    }

    private String fetch(String url, String baseDate, String baseTime, int nx, int ny) {
        try {
            // apiKey는 .env에서 이미 URL 인코딩된 상태로 넘어오므로
            // UriComponentsBuilder에 넘기면 이중 인코딩이 발생한다.
            // serviceKey만 URL에 직접 삽입하고 나머지는 Builder로 구성한다.
            String otherParams = UriComponentsBuilder.newInstance()
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1000)
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", baseDate)
                    .queryParam("base_time", baseTime)
                    .queryParam("nx", nx)
                    .queryParam("ny", ny)
                    .build()
                    .getQuery();

            URI uri = URI.create(url + "?serviceKey=" + apiKey + "&" + otherParams);

            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw new IllegalStateException("기상청 API 호출에 실패했습니다. (URL: " + url + ")", ex);
        }
    }

    private WeatherBriefingWithForecast parseWeatherData(String regionName, String ncstBody, String fcstBody) {
        try {
            // A. 초단기예보(Future) 파싱
            JsonNode fcstRoot = objectMapper.readTree(fcstBody);
            JsonNode fcstItems = fcstRoot.path("response").path("body").path("items").path("item");
            if (!fcstItems.isArray()) {
                throw new IllegalArgumentException("초단기예보 데이터가 존재하지 않습니다.");
            }

            Map<String, ForecastTemp> forecastMap = new LinkedHashMap<>();
            for (JsonNode item : fcstItems) {
                String fDate = item.path("fcstDate").asText();
                String fTime = item.path("fcstTime").asText();
                String key = fDate + fTime;

                ForecastTemp ft = forecastMap.computeIfAbsent(key, k -> {
                    ForecastTemp newFt = new ForecastTemp();
                    newFt.time = fTime.substring(0, 2) + ":" + fTime.substring(2, 4);
                    return newFt;
                });

                String category = item.path("category").asText();
                String val = item.path("fcstValue").asText();

                if ("T1H".equals(category)) {
                    ft.temp = (int) Math.round(Double.parseDouble(val));
                } else if ("SKY".equals(category)) {
                    ft.sky = val;
                } else if ("PTY".equals(category)) {
                    ft.pty = val;
                }
            }

            // 시간 순서대로 정렬하여 6개 가져오기
            List<WeatherForecastResult> forecasts = new ArrayList<>();
            List<String> sortedKeys = new ArrayList<>(forecastMap.keySet());
            Collections.sort(sortedKeys);

            for (String key : sortedKeys) {
                if (forecasts.size() >= HOURLY_FORECAST_LIMIT) {
                    break;
                }
                ForecastTemp ft = forecastMap.get(key);
                if (ft.temp != null) {
                    forecasts.add(new WeatherForecastResult(
                            ft.time,
                            ft.temp,
                            mapCondition(ft.sky, ft.pty),
                            mapRainChance(ft.pty)
                    ));
                }
            }

            // B. 초단기실황(Current) 파싱
            JsonNode ncstRoot = objectMapper.readTree(ncstBody);
            JsonNode ncstItems = ncstRoot.path("response").path("body").path("items").path("item");
            if (!ncstItems.isArray()) {
                throw new IllegalArgumentException("초단기실황 데이터가 존재하지 않습니다.");
            }

            Integer currentTemp = null;
            String currentPty = "0";
            for (JsonNode item : ncstItems) {
                String category = item.path("category").asText();
                String val = item.path("obsrValue").asText();
                if ("T1H".equals(category)) {
                    currentTemp = (int) Math.round(Double.parseDouble(val));
                } else if ("PTY".equals(category)) {
                    currentPty = val;
                }
            }

            if (currentTemp == null) {
                currentTemp = forecasts.isEmpty() ? 22 : forecasts.get(0).temperature();
            }

            // 현재 날씨 기상 텍스트 판단
            String currentCondition = "맑음";
            if (!"0".equals(currentPty)) {
                currentCondition = mapPty(currentPty);
            } else if (!forecasts.isEmpty()) {
                currentCondition = forecasts.get(0).condition();
            }

            List<Integer> temps = forecasts.stream()
                    .map(WeatherForecastResult::temperature)
                    .filter(Objects::nonNull)
                    .toList();
            int tempMin = temps.stream().min(Integer::compareTo).orElse(currentTemp);
            int tempMax = temps.stream().max(Integer::compareTo).orElse(currentTemp);

            WeatherBriefingResult current = new WeatherBriefingResult(
                    regionName,
                    currentCondition,
                    currentTemp,
                    mapRainChance(currentPty),
                    "05:23", // 일출 기본값
                    "19:33", // 일몰 기본값
                    tempMin,
                    tempMax
            );

            return new WeatherBriefingWithForecast(current, forecasts);
        } catch (Exception ex) {
            throw new IllegalStateException("기상청 날씨 응답 파싱에 실패했습니다.", ex);
        }
    }

    private static String mapCondition(String sky, String pty) {
        if (!"0".equals(pty)) {
            return mapPty(pty);
        }
        return switch (sky) {
            case "1" -> "맑음";
            case "3" -> "구름 많음";
            case "4" -> "흐림";
            default -> "맑음";
        };
    }

    private static String mapPty(String pty) {
        return switch (pty) {
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            case "5" -> "비"; // 이슬비 -> 비로 퉁침
            case "6" -> "진눈깨비";
            case "7" -> "눈날림";
            default -> "맑음";
        };
    }

    private static Integer mapRainChance(String pty) {
        return "0".equals(pty) ? 0 : 80;
    }

    // --- 시간 계산 헬퍼 구조체 ---
    private static class KmaTime {
        final String baseDate;
        final String baseTime;

        KmaTime(String baseDate, String baseTime) {
            this.baseDate = baseDate;
            this.baseTime = baseTime;
        }
    }

    private static KmaTime calculateNcstTime(LocalDateTime now) {
        // 초단기실황: 매시 40분 발표. 40분 미만이면 이전 시간 데이터 조회
        LocalDateTime target = now.getMinute() < 40 ? now.minusHours(1) : now;
        return new KmaTime(
                target.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                target.format(DateTimeFormatter.ofPattern("HH00"))
        );
    }

    private static KmaTime calculateFcstTime(LocalDateTime now) {
        // 초단기예보: 매시 45분 발표. 45분 미만이면 이전 시간 데이터 조회
        LocalDateTime target = now.getMinute() < 45 ? now.minusHours(1) : now;
        return new KmaTime(
                target.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                target.format(DateTimeFormatter.ofPattern("HH30"))
        );
    }

    // --- 위경도 격자좌표 변환 헬퍼 ---
    private static class GridPoint {
        final int nx;
        final int ny;

        GridPoint(int nx, int ny) {
            this.nx = nx;
            this.ny = ny;
        }
    }

    private static GridPoint convertToGrid(double lat, double lon) {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기준점 Y좌표(GRID)

        double DEGRAD = Math.PI / 180.0;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        int nx = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
        int ny = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

        return new GridPoint(nx, ny);
    }

    private static class ForecastTemp {
        String time;
        Integer temp;
        String sky = "1";
        String pty = "0";
    }
}
