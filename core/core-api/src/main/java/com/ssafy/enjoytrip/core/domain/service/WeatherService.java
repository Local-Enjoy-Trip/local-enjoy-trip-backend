package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import com.ssafy.enjoytrip.core.domain.external.WeatherClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private static final List<WeatherSummary> FALLBACK_BRIEFINGS = List.of(
            new WeatherSummary("서울", "맑음", 22, 10, "05:23", "19:33"),
            new WeatherSummary("부산", "구름 조금", 21, 20, "05:17", "19:22"),
            new WeatherSummary("제주", "바람 강함", 23, 30, "05:35", "19:25")
    );

    private final WeatherClient weatherClient;

    public List<WeatherSummary> findWeatherBriefings() {
        return completeWithFallback(weatherClient.findWeatherBriefings());
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
