package com.ssafy.enjoytrip.external;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClickHouseAttractionPopularityRepositoryTest {

    @DisplayName("클릭하우스 연결 실패는 빈 인기 맵으로 폴백한다")
    @Test
    void returnsEmptyMapWhenClickHouseConnectionFails() {
        ClickHousePopularityProperties properties = new ClickHousePopularityProperties();
        properties.setUrl("jdbc:clickhouse://127.0.0.1:1/default");
        properties.setUsername("default");
        properties.setPassword("wrong");
        properties.setQueryTimeout(Duration.ofSeconds(1));
        ClickHouseAttractionPopularityRepository repository = new ClickHouseAttractionPopularityRepository(properties);

        Map<Long, Long> result = repository.findFavoriteCounts(List.of(1L, 2L));

        assertThat(result).isEmpty();
    }

    @DisplayName("빈 후보 목록은 클릭하우스 조회 없이 빈 맵을 반환한다")
    @Test
    void returnsEmptyMapForEmptyCandidates() {
        ClickHouseAttractionPopularityRepository repository = new ClickHouseAttractionPopularityRepository(
                new ClickHousePopularityProperties()
        );

        Map<Long, Long> result = repository.findFavoriteCounts(List.of());

        assertThat(result).isEmpty();
    }
}
