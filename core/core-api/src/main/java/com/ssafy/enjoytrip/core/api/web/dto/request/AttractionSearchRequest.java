package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.query.AttractionSearchCondition;
import io.swagger.v3.oas.annotations.media.Schema;

public record AttractionSearchRequest(
        @Schema(description = "현재 위치 경도", example = "126.9780")
        String mapX,

        @Schema(description = "현재 위치 위도", example = "37.5665")
        String mapY,

        @Schema(description = "좌표 검색 반경(m), 기본값 3000", example = "3000")
        String radius,

        @Schema(description = "관광 타입 ID", example = "12")
        String contentTypeId,

        @Schema(description = "관광지 검색어", example = "경복궁")
        String keyword,

        @Schema(description = "시도 코드", example = "1")
        String sidoCode,

        @Schema(description = "구군 코드", example = "1")
        String gugunCode
) {
    private static final double DEFAULT_RADIUS_METERS = 3000.0;
    private static final double MAX_RADIUS_METERS = 20000.0;

    public AttractionSearchCondition toCondition() {
        Double longitude = parseDouble(mapX);
        Double latitude = parseDouble(mapY);
        boolean aroundSearch = longitude != null && latitude != null;

        return new AttractionSearchCondition(
                aroundSearch ? null : parseInteger(sidoCode),
                aroundSearch ? null : parseInteger(gugunCode),
                trimToNull(contentTypeId),
                trimToNull(keyword),
                longitude,
                latitude,
                aroundSearch ? parseRadius(radius) : null
        );
    }

    private static Integer parseInteger(String value) {
        try {
            String normalized = trimToNull(value);
            if (normalized == null) {
                return null;
            }

            return Integer.parseInt(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Double parseDouble(String value) {
        try {
            String normalized = trimToNull(value);
            if (normalized == null) {
                return null;
            }

            double parsed = Double.parseDouble(normalized);
            if (!Double.isFinite(parsed)) {
                return null;
            }

            return parsed;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static double parseRadius(String value) {
        Double parsed = parseDouble(value);
        if (parsed == null || parsed <= 0) {
            return DEFAULT_RADIUS_METERS;
        }

        return Math.min(parsed, MAX_RADIUS_METERS);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
