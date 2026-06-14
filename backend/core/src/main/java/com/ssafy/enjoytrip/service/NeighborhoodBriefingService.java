package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.CourseBriefingCandidate;
import com.ssafy.enjoytrip.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.domain.NeighborhoodBriefingPrompt;
import com.ssafy.enjoytrip.domain.WeatherSummary;
import com.ssafy.enjoytrip.repository.CourseRepository;
import com.ssafy.enjoytrip.repository.NeighborhoodBriefingGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NeighborhoodBriefingService {
    private static final int COURSE_CANDIDATE_LIMIT = 3;
    private final WeatherService weatherService;
    private final CourseRepository courseRepository;
    private final NeighborhoodBriefingGenerator generator;

    public NeighborhoodBriefing brief(String regionName) {
        WeatherSummary weather = findWeatherForRegion(regionName);
        List<CourseBriefingCandidate> candidates = findCourseCandidates(regionName);

        if (candidates.isEmpty()) {
            return fallbackBriefing(regionName, weather, candidates);
        }

        NeighborhoodBriefingPrompt prompt = new NeighborhoodBriefingPrompt(
                regionName,
                weather,
                candidates
        );

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
        return courseRepository.findPublicReadyBriefingCandidates(region, COURSE_CANDIDATE_LIMIT);
    }

    private NeighborhoodBriefing fallbackBriefing(String region,
                                                  WeatherSummary weather,
                                                  List<CourseBriefingCandidate> candidates) {
        String briefing = candidates.isEmpty()
                ? weatherOnlyFallback(region, weather)
                : courseFallback(region, weather, candidates.getFirst());

        return new NeighborhoodBriefing(region, briefing);
    }

    private static String weatherOnlyFallback(String region, WeatherSummary weather) {
        return "오늘 %s은 %s이고 기온은 %d도예요. 저장된 코스가 생기면 날씨에 맞춰 추천해드릴게요."
                .formatted(region, weather.condition(), weather.temperature());
    }

    private static String courseFallback(String region,
                                         WeatherSummary weather,
                                         CourseBriefingCandidate candidate) {
        return "오늘 %s은 %s이고 기온은 %d도예요. 저장된 %s 코스 어떠세요?"
                .formatted(region, weather.condition(), weather.temperature(), candidate.title());
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
