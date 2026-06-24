package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.core.domain.WeatherForecast;
import com.ssafy.enjoytrip.core.domain.WeatherWithForecast;
import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import com.ssafy.enjoytrip.external.WeatherBriefingResult;
import com.ssafy.enjoytrip.external.briefing.LocalPlaceData;
import com.ssafy.enjoytrip.external.briefing.NeighborhoodBriefingPromptData;
import com.ssafy.enjoytrip.external.briefing.SpringAiNeighborhoodBriefingGenerator;
import com.ssafy.enjoytrip.storage.db.core.model.LocalPlaceRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NeighborhoodBriefingMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NeighborhoodBriefingService {
    private static final int LOCAL_PLACE_LIMIT = 5;

    private final SpringAiNeighborhoodBriefingGenerator generator;
    private final NeighborhoodBriefingMapper neighborhoodBriefingMapper;

    @Cacheable(cacheNames = "neighborhoodBriefings", key = "#regionName + ':' + #currentHour")
    public NeighborhoodBriefing brief(String regionName, WeatherWithForecast weatherWithForecast, String currentHour) {
        WeatherSummary weather = weatherWithForecast.current();
        List<WeatherForecast> forecasts = weatherWithForecast.forecasts();

        List<LocalPlaceData> localPlaces = findLocalPlaces(regionName);

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
                localPlaces
        );

        String generated = normalizeGeneratedBriefing(generator.generate(prompt));
        if (generated.isBlank()) {
            return fallbackBriefing(regionName, weather, forecasts);
        }

        return new NeighborhoodBriefing(regionName, generated, weather, forecasts);
    }

    private List<LocalPlaceData> findLocalPlaces(String regionName) {
        return neighborhoodBriefingMapper.findLocalPlaces(regionName, LOCAL_PLACE_LIMIT)
                .stream()
                .map(record -> new LocalPlaceData(record.title(), record.addr1(), record.contentTypeId()))
                .toList();
    }

    private NeighborhoodBriefing fallbackBriefing(String region,
                                                  WeatherSummary weather,
                                                  List<WeatherForecast> forecasts) {
        return new NeighborhoodBriefing(
                region,
                "오늘 %s은 %s이고 기온은 %d도예요.\n%s 동네 곳곳을 구경해보세요."
                        .formatted(region, weather.condition(), weather.temperature(), region),
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
        return normalized.length() > 200 ? normalized.substring(0, 200).strip() : normalized;
    }
}
