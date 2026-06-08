package com.ssafy.enjoytrip.external;

import com.ssafy.enjoytrip.domain.Attraction;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
public class LocalAttractionClient {
    private static final String DATA_PATH = "data/전국관광지정보표준데이터.json";
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile List<Attraction> cache;

    public List<Attraction> search(String areaCode, String sigunguCode, String contentTypeId, String keyword) {
        return attractions().stream()
                .filter(item -> matchesArea(item, areaCode))
                .filter(item -> matchesSigungu(item, sigunguCode))
                .filter(item -> matchesContentType(item, contentTypeId))
                .filter(item -> matchesKeyword(item, keyword))
                .toList();
    }

    public List<Attraction> searchAround(String mapX, String mapY, String radius, String contentTypeId, String keyword) {
        double longitude = parseDouble(mapX);
        double latitude = parseDouble(mapY);
        double maxDistance = parseDouble(radius);
        if (maxDistance <= 0) {
            maxDistance = 3_000.0;
        }
        if (latitude == 0.0 || longitude == 0.0) {
            return search("", "", contentTypeId, keyword);
        }

        double finalMaxDistance = maxDistance;
        return attractions().stream()
                .filter(item -> matchesContentType(item, contentTypeId))
                .filter(item -> matchesKeyword(item, keyword))
                .filter(item -> item.latitude() != null && item.longitude() != null)
                .filter(item -> distanceMeters(latitude, longitude, item.latitude(), item.longitude()) <= finalMaxDistance)
                .toList();
    }

    private List<Attraction> attractions() {
        List<Attraction> loaded = cache;
        if (loaded != null) {
            return loaded;
        }
        synchronized (this) {
            if (cache == null) {
                cache = load();
            }
            return cache;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Attraction> load() {
        ClassPathResource resource = new ClassPathResource(DATA_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Object> root = objectMapper.readValue(inputStream, Map.class);
            Object records = root.get("records");
            if (!(records instanceof List<?> rows)) {
                return List.of();
            }
            long[] index = {1L};
            return rows.stream()
                    .filter(Map.class::isInstance)
                    .map(row -> toAttraction((Map<String, Object>) row, index[0]++))
                    .filter(item -> !item.title().isBlank())
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Local attraction data is not available: " + DATA_PATH, ex);
        }
    }

    private Attraction toAttraction(Map<String, Object> row, long index) {
        String title = text(row, "관광지명");
        String roadAddress = text(row, "소재지도로명주소");
        String jibunAddress = text(row, "소재지지번주소");
        String address = jibunAddress;
        if (!roadAddress.isBlank()) {
            address = roadAddress;
        }
        String provider = text(row, "제공기관명");
        String areaBasis = address + " " + provider;
        Double latitude = nullableDouble(text(row, "위도"));
        Double longitude = nullableDouble(text(row, "경도"));

        return new Attraction(
                index,
                title,
                address,
                alternateAddress(roadAddress, jibunAddress),
                "",
                text(row, "관리기관전화번호"),
                "",
                "",
                0,
                areaCode(areaBasis),
                0,
                latitude,
                longitude,
                "",
                "12",
                text(row, "관광지소개"),
                0,
                0.0,
                0,
                List.of(),
                false,
                null
        );
    }

    private static boolean matchesArea(Attraction item, String areaCode) {
        if (isBlank(areaCode)) {
            return true;
        }
        return Objects.equals(item.sidoCode(), parseInt(areaCode));
    }

    private static boolean matchesSigungu(Attraction item, String sigunguCode) {
        if (isBlank(sigunguCode)) {
            return true;
        }
        return Objects.equals(item.gugunCode(), parseInt(sigunguCode));
    }

    private static boolean matchesContentType(Attraction item, String contentTypeId) {
        return isBlank(contentTypeId) || contentTypeId.trim().equals(item.contentTypeId());
    }

    private static boolean matchesKeyword(Attraction item, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        String query = keyword.trim().toLowerCase(Locale.ROOT);
        String searchable = (value(item.title()) + " " + value(item.addr1()) + " " + value(item.addr2()) + " " + value(item.overview()))
                .toLowerCase(Locale.ROOT);
        return searchable.contains(query);
    }

    private static String alternateAddress(String roadAddress, String jibunAddress) {
        if (!roadAddress.isBlank() && !jibunAddress.isBlank()) {
            return jibunAddress;
        }
        return "";
    }

    private static Integer areaCode(String value) {
        if (value.contains("서울")) return 1;
        if (value.contains("인천")) return 2;
        if (value.contains("대전")) return 3;
        if (value.contains("대구")) return 4;
        if (value.contains("광주")) return 5;
        if (value.contains("부산")) return 6;
        if (value.contains("울산")) return 7;
        if (value.contains("세종")) return 8;
        if (value.contains("경기")) return 31;
        if (value.contains("강원")) return 32;
        if (value.contains("충북") || value.contains("충청북도")) return 33;
        if (value.contains("충남") || value.contains("충청남도")) return 34;
        if (value.contains("경북") || value.contains("경상북도")) return 35;
        if (value.contains("경남") || value.contains("경상남도")) return 36;
        if (value.contains("전북") || value.contains("전라북도")) return 37;
        if (value.contains("전남") || value.contains("전라남도")) return 38;
        if (value.contains("제주")) return 39;
        return 0;
    }

    private static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(rLat1) * Math.cos(rLat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            return "";
        }
        return String.valueOf(value).trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String value(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private static Integer parseInt(String value) {
        try {
            if (isBlank(value)) {
                return 0;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static double parseDouble(String value) {
        try {
            if (isBlank(value)) {
                return 0.0;
            }
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private static Double nullableDouble(String value) {
        if (isBlank(value)) {
            return 0.0;
        }
        return parseDouble(value);
    }
}
