package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.WeatherSummary;
import com.ssafy.enjoytrip.repository.WeatherRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherServiceTest {

    @Test
    void returnsDefaultFallbackRowsWhenRepositoryReturnsEmpty() {
        WeatherService service = new WeatherService(() -> List.of());

        List<WeatherSummary> result = service.findWeatherBriefings();

        assertDefaultFallbackRows(result);
    }

    @Test
    void returnsDefaultFallbackRowsWhenRepositoryThrows() {
        WeatherService service = new WeatherService(() -> {
            throw new IllegalStateException("KMA unavailable");
        });

        List<WeatherSummary> result = service.findWeatherBriefings();

        assertDefaultFallbackRows(result);
    }

    @Test
    void preservesLiveRowsAndFillsMissingDefaultRegions() {
        WeatherSummary liveSeoul = new WeatherSummary("서울", "비", 18, 80, null, null);
        WeatherService service = new WeatherService(() -> List.of(liveSeoul));

        List<WeatherSummary> result = service.findWeatherBriefings();

        assertEquals(3, result.size());
        assertEquals(new WeatherSummary("서울", "비", 18, 80, "05:23", "19:33"), result.get(0));
        assertEquals(new WeatherSummary("부산", "구름 조금", 21, 20, "05:17", "19:22"), result.get(1));
        assertEquals(new WeatherSummary("제주", "바람 강함", 23, 30, "05:35", "19:25"), result.get(2));
    }

    @Test
    void keepsDefaultOrderingWhenRepositoryReturnsAllDefaultRegions() {
        WeatherRepository repository = () -> List.of(
                new WeatherSummary("제주", "흐림", 24, 40, null, null),
                new WeatherSummary("부산", "맑음", 20, 0, null, null),
                new WeatherSummary("서울", "구름 많음", 19, 30, null, null)
        );
        WeatherService service = new WeatherService(repository);

        List<WeatherSummary> result = service.findWeatherBriefings();

        assertEquals(List.of("서울", "부산", "제주"), result.stream().map(WeatherSummary::region).toList());
        assertEquals("구름 많음", result.get(0).condition());
        assertEquals("맑음", result.get(1).condition());
        assertEquals("흐림", result.get(2).condition());
    }

    private static void assertDefaultFallbackRows(List<WeatherSummary> result) {
        assertEquals(List.of(
                new WeatherSummary("서울", "맑음", 22, 10, "05:23", "19:33"),
                new WeatherSummary("부산", "구름 조금", 21, 20, "05:17", "19:22"),
                new WeatherSummary("제주", "바람 강함", 23, 30, "05:35", "19:25")
        ), result);
    }
}
