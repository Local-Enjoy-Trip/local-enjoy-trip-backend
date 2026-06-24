package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.CourseBriefingCandidate;
import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.core.domain.WeatherForecast;
import com.ssafy.enjoytrip.core.domain.WeatherWithForecast;
import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import com.ssafy.enjoytrip.external.WeatherBriefingResult;
import com.ssafy.enjoytrip.external.briefing.CourseBriefingCandidateData;
import com.ssafy.enjoytrip.external.briefing.NeighborhoodBriefingPromptData;
import com.ssafy.enjoytrip.external.briefing.SpringAiNeighborhoodBriefingGenerator;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NeighborhoodBriefingMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NeighborhoodBriefingService {
    private static final int COURSE_CANDIDATE_LIMIT = 3;
    private final WeatherService weatherService;
    private final SpringAiNeighborhoodBriefingGenerator generator;
    private final NeighborhoodBriefingMapper neighborhoodBriefingMapper;

    @Cacheable(cacheNames = "neighborhoodBriefings", key = "#regionName + ':' + #currentHour")
    public NeighborhoodBriefing brief(String regionName, Double latitude, Double longitude, String currentHour) {
        WeatherWithForecast weatherWithForecast = weatherService.findWeatherWithForecast(latitude, longitude, regionName);
        WeatherSummary weather = weatherWithForecast.current();
        List<WeatherForecast> forecasts = weatherWithForecast.forecasts();

        List<CourseBriefingCandidate> candidates = findCourseCandidates(regionName);

        if (candidates.isEmpty()) {
            return fallbackBriefing(regionName, weather, forecasts, candidates);
        }

        NeighborhoodBriefingPromptData prompt = new NeighborhoodBriefingPromptData(
                regionName,
                new WeatherBriefingResult(
                        weather.region(),
                        weather.condition(),
                        weather.temperature(),
                        weather.rainChance(),
                        weather.sunrise(),
                        weather.sunset(),
                        weather.tempMin(),
                        weather.tempMax()
                ),
                candidates.stream()
                        .map(candidate -> new CourseBriefingCandidateData(
                                candidate.id(),
                                candidate.title(),
                                candidate.regionName()
                        ))
                        .toList()
        );
        String generated = normalizeGeneratedBriefing(generator.generate(prompt));
        if (generated.isBlank()) {
            return fallbackBriefing(regionName, weather, forecasts, candidates);
        }

        return new NeighborhoodBriefing(regionName, generated, weather, forecasts);
    }

    private List<CourseBriefingCandidate> findCourseCandidates(String region) {
        if (COURSE_CANDIDATE_LIMIT <= 0) {
            return List.of();
        }

        return neighborhoodBriefingMapper.findPublicReadyCandidates(region, COURSE_CANDIDATE_LIMIT).stream()
                .map(record -> new CourseBriefingCandidate(record.id(), record.title(), record.regionName()))
                .toList();
    }

    private NeighborhoodBriefing fallbackBriefing(String region,
                                                  WeatherSummary weather,
                                                  List<WeatherForecast> forecasts,
                                                  List<CourseBriefingCandidate> candidates) {
        if (candidates.isEmpty()) {
            return new NeighborhoodBriefing(
                    region,
                    "오늘 %s은 %s이고 기온은 %d도예요. 저장된 코스가 생기면 날씨에 맞춰 추천해드릴게요."
                            .formatted(region, weather.condition(), weather.temperature()),
                    weather,
                    forecasts
            );
        }

        return new NeighborhoodBriefing(
                region,
                "오늘 %s은 %s이고 기온은 %d도예요. 저장된 %s 코스 어떠세요?"
                        .formatted(
                                region,
                                weather.condition(),
                                weather.temperature(),
                                candidates.getFirst().title()
                        ),
                weather,
                forecasts
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

