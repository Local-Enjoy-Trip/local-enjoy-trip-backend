package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.CourseBriefingCandidate;
import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefingPrompt;
import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import com.ssafy.enjoytrip.external.briefing.SpringAiNeighborhoodBriefingGenerator;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NeighborhoodBriefingMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NeighborhoodBriefingService {
    private static final int COURSE_CANDIDATE_LIMIT = 3;
    private final WeatherService weatherService;
    private final SpringAiNeighborhoodBriefingGenerator generator;
    private final NeighborhoodBriefingMapper neighborhoodBriefingMapper;

    public NeighborhoodBriefing brief(String regionName) {
        WeatherSummary weather = findWeatherForRegion(regionName);
        List<CourseBriefingCandidate> candidates = findCourseCandidates(regionName);

        if (candidates.isEmpty()) {
            return fallbackBriefing(regionName, weather, candidates);
        }

        NeighborhoodBriefingPrompt prompt = new NeighborhoodBriefingPrompt(regionName, weather, candidates);
        String generated = normalizeGeneratedBriefing(generator.generate(prompt));
        if (generated.isBlank()) {
            return fallbackBriefing(regionName, weather, candidates);
        }

        return new NeighborhoodBriefing(regionName, generated);
    }

    private WeatherSummary findWeatherForRegion(String region) {
        List<WeatherSummary> briefings = weatherService.findWeatherBriefings();

        return briefings.stream()
                .filter(briefing -> region.equals(briefing.region()))
                .findFirst()
                .or(() -> briefings.stream().findFirst())
                .orElse(new WeatherSummary(region, "맑음", 22, 10, "05:23", "19:33"));
    }

    private List<CourseBriefingCandidate> findCourseCandidates(String region) {
        if (COURSE_CANDIDATE_LIMIT <= 0) {
            return List.of();
        }

        return neighborhoodBriefingMapper.findPublicReadyCandidates(region, COURSE_CANDIDATE_LIMIT).stream()
                .map(row -> new CourseBriefingCandidate(row.id(), row.title(), row.regionName()))
                .toList();
    }

    private NeighborhoodBriefing fallbackBriefing(String region,
                                                  WeatherSummary weather,
                                                  List<CourseBriefingCandidate> candidates) {
        if (candidates.isEmpty()) {
            return new NeighborhoodBriefing(
                    region,
                    "오늘 %s은 %s이고 기온은 %d도예요. 저장된 코스가 생기면 날씨에 맞춰 추천해드릴게요."
                            .formatted(region, weather.condition(), weather.temperature())
            );
        }

        return new NeighborhoodBriefing(
                region,
                "오늘 %s은 %s이고 기온은 %d도예요. 저장된 %s 코스 어떠세요?"
                        .formatted(region, weather.condition(), weather.temperature(), candidates.getFirst().title())
        );
    }

    private static String normalizeGeneratedBriefing(String generated) {
        if (generated == null) {
            return "";
        }

        String normalized = generated.strip()
                .replaceAll("[\t ]+", " ")
                .replaceAll(" *[\r\n]+ *", "\n");
        return normalized.length() > 160 ? normalized.substring(0, 160).strip() : normalized;
    }
}
