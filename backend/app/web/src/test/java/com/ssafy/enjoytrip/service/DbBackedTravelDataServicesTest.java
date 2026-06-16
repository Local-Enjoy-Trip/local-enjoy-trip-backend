package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.dto.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.ChargerItem;
import com.ssafy.enjoytrip.domain.NewsItem;
import com.ssafy.enjoytrip.repository.AttractionRepository;
import com.ssafy.enjoytrip.repository.ChargerRepository;
import com.ssafy.enjoytrip.repository.NewsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("service")
class DbBackedTravelDataServicesTest {

    @Nested
    class AttractionServiceTests {
        private final AttractionRepository repository = mock(AttractionRepository.class);
        private final AttractionService service = new AttractionService(repository, attractionIds -> Map.of());

        @DisplayName("지역 검색을 저장소에 위임한다")
        @Test
        void delegatesAreaSearchToRepository() {
            Attraction attraction = attraction(1L, "경복궁");
            AttractionSearchCondition condition = new AttractionSearchCondition("1", "2", "12", "궁", "", "", "");
            when(repository.search(condition)).thenReturn(List.of(attraction));

            List<Attraction> result = service.searchAttractions(condition);

            assertThat(result).containsExactly(attraction);
            verify(repository).search(condition);
        }

        @DisplayName("주변 검색을 저장소에 위임한다")
        @Test
        void delegatesAroundSearchToRepository() {
            Attraction attraction = attraction(2L, "한강공원");
            AttractionSearchCondition condition = new AttractionSearchCondition("", "", "12", "공원", "126.9", "37.5", "1000");
            when(repository.search(condition)).thenReturn(List.of(attraction));

            List<Attraction> result = service.searchAttractions(condition);

            assertThat(result).containsExactly(attraction);
            verify(repository).search(condition);
        }

        private Attraction attraction(Long id, String title) {
            return new Attraction(
                    id, title, "addr1", "addr2", "zip", "tel", "image1", "image2",
                    7, 1, 2, 37.5, 126.9, "6", "12", "overview",
                    0, 0.0, 0, List.of(), false, null
            );
        }
    }

    @Nested
    class EvChargerServiceTests {
        private final ChargerRepository repository = mock(ChargerRepository.class);
        private final EvChargerService service = new EvChargerService(repository);

        @DisplayName("충전소 검색을 저장소에 위임한다")
        @Test
        void delegatesChargerSearchToRepository() {
            ChargerItem charger = new ChargerItem(
                    "ST001", "서울충전소", "01", "06", "서울", "지하1층",
                    37.5, 127.0, "24시간", "환경부", "1661-9408", "2");
            when(repository.findChargers("11", "서울", 2, 50)).thenReturn(List.of(charger));

            List<ChargerItem> result = service.findChargers("11", "서울", 2, 50);

            assertThat(result).containsExactly(charger);
            verify(repository).findChargers("11", "서울", 2, 50);
        }
    }

    @Nested
    class NewsServiceTests {
        private final NewsRepository repository = mock(NewsRepository.class);
        private final NewsService service = new NewsService(repository);

        @DisplayName("뉴스 조회를 저장소에 위임한다")
        @Test
        void delegatesNewsLookupToRepository() {
            NewsItem news = new NewsItem("n1", "관광 뉴스", "https://example.com", "요약", "출처", "2026-05-15");
            when(repository.findNews()).thenReturn(List.of(news));

            List<NewsItem> result = service.findNews();

            assertThat(result).containsExactly(news);
            verify(repository).findNews();
        }
    }
}
