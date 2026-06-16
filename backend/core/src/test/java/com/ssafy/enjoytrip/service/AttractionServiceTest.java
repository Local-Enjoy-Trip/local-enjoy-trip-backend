package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.application.dto.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.AttractionStats;
import com.ssafy.enjoytrip.domain.AttractionTag;
import com.ssafy.enjoytrip.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.application.dto.query.NearbySearchCondition;
import com.ssafy.enjoytrip.domain.PopularAttraction;
import com.ssafy.enjoytrip.repository.AttractionPopularityRepository;
import com.ssafy.enjoytrip.repository.AttractionRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttractionServiceTest {

    @DisplayName("클릭하우스 집계가 있으면 인기 수 내림차순과 거리 및 안정 키로 정렬한다")
    @Test
    void sortsByClickHousePopularityWhenAnyAggregateExists() {
        NearbySearchCondition condition = new NearbySearchCondition(126.9780, 37.5665, 500, 20);
        FakeAttractionRepository repository = new FakeAttractionRepository(List.of(
                candidate(1L, "가까운 낮은 인기", 10.0),
                candidate(2L, "먼 높은 인기", 90.0),
                candidate(3L, "무집계 후보", 5.0)
        ));
        AttractionPopularityRepository popularityRepository = ids -> Map.of(2L, 7L, 1L, 3L);
        AttractionService service = new AttractionService(repository, popularityRepository);

        List<PopularAttraction> result = service.findPopularNearbyAttractions(condition, "ssafy");

        assertEquals(List.of(2L, 1L, 3L), result.stream().map(popular -> popular.attraction().id()).toList());
        assertEquals(List.of(7L, 3L, 0L), result.stream().map(PopularAttraction::popularityCount).toList());
        assertEquals(condition, repository.lastCondition);
        assertEquals("ssafy", repository.lastUserId);
    }

    @DisplayName("클릭하우스 집계가 비어 있으면 저장소의 기본 주변 순서를 그대로 보존한다")
    @Test
    void keepsBaseNearbyOrderWhenClickHouseReturnsEmptyMap() {
        NearbySearchCondition condition = new NearbySearchCondition(126.9780, 37.5665, 500, 20);
        FakeAttractionRepository repository = new FakeAttractionRepository(List.of(
                candidate(1L, "거리순 첫째", 10.0),
                candidate(2L, "거리순 둘째", 20.0)
        ));
        AttractionService service = new AttractionService(repository, ids -> Map.of());

        List<PopularAttraction> result = service.findPopularNearbyAttractions(condition, "");

        assertEquals(List.of(1L, 2L), result.stream().map(popular -> popular.attraction().id()).toList());
        assertEquals(List.of(0L, 0L), result.stream().map(PopularAttraction::popularityCount).toList());
    }

    @DisplayName("동일 인기 수에서는 거리와 제목 및 식별자로 결정적으로 정렬한다")
    @Test
    void usesDeterministicTieBreakersForEqualPopularity() {
        NearbySearchCondition condition = new NearbySearchCondition(126.9780, 37.5665, 500, 20);
        FakeAttractionRepository repository = new FakeAttractionRepository(List.of(
                candidate(3L, "나", 20.0),
                candidate(2L, "가", 20.0),
                candidate(1L, "다", 10.0)
        ));
        AttractionService service = new AttractionService(repository, ids -> Map.of(1L, 5L, 2L, 5L, 3L, 5L));

        List<PopularAttraction> result = service.findPopularNearbyAttractions(condition, "");

        assertEquals(List.of(1L, 2L, 3L), result.stream().map(popular -> popular.attraction().id()).toList());
    }

    private static NearbyAttractionCandidate candidate(Long id, String title, double distanceMeters) {
        return new NearbyAttractionCandidate(attraction(id, title), distanceMeters);
    }

    private static Attraction attraction(Long id, String title) {
        return new Attraction(
                id, title, "addr1", "addr2", "zip", "tel", "image1", "image2",
                7, 1, 2, 37.5, 126.9, "6", "12", "overview",
                11, 4.5, 2, List.of(), false, null
        );
    }

    private static class FakeAttractionRepository implements AttractionRepository {
        private final List<NearbyAttractionCandidate> candidates;
        private NearbySearchCondition lastCondition;
        private String lastUserId;

        private FakeAttractionRepository(List<NearbyAttractionCandidate> candidates) {
            this.candidates = candidates;
        }

        @Override
        public List<Attraction> search(AttractionSearchCondition condition) {
            return List.of();
        }

        @Override
        public List<NearbyAttractionCandidate> findNearbyCandidates(NearbySearchCondition condition, String userId) {
            lastCondition = condition;
            lastUserId = userId;
            return candidates;
        }

        @Override
        public boolean existsById(Long attractionId) {
            return false;
        }

        @Override
        public AttractionStats findStats(Long attractionId, String userId) {
            return null;
        }

        @Override
        public void addFavorite(Long attractionId, String userId) {
        }

        @Override
        public boolean removeFavorite(Long attractionId, String userId) {
            return false;
        }

        @Override
        public void upsertRating(Long attractionId, String userId, int rating) {
        }

        @Override
        public boolean removeRating(Long attractionId, String userId) {
            return false;
        }

        @Override
        public List<AttractionTag> findAllTags() {
            return List.of();
        }

        @Override
        public AttractionTag insertTag(String name) {
            return null;
        }

        @Override
        public boolean updateTag(Long tagId, String name) {
            return false;
        }

        @Override
        public boolean deleteTag(Long tagId) {
            return false;
        }

        @Override
        public boolean replaceTags(Long attractionId, List<Long> tagIds) {
            return false;
        }
    }
}
