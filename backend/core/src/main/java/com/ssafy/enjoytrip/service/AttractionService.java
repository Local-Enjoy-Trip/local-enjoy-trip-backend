package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.dto.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.AttractionStats;
import com.ssafy.enjoytrip.domain.AttractionTag;
import com.ssafy.enjoytrip.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.dto.query.NearbySearchCondition;
import com.ssafy.enjoytrip.domain.PopularAttraction;
import com.ssafy.enjoytrip.repository.AttractionPopularityRepository;
import com.ssafy.enjoytrip.repository.AttractionRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttractionService {
    private final AttractionRepository repository;
    private final AttractionPopularityRepository popularityRepository;

    public List<Attraction> searchAttractions(AttractionSearchCondition condition) {
        return repository.search(condition);
    }

    public List<Attraction> searchAttractions(AttractionSearchCondition condition, String userId) {
        return repository.search(condition, userId);
    }

    public List<PopularAttraction> findPopularNearbyAttractions(NearbySearchCondition condition,
                                                                String userId) {
        List<NearbyAttractionCandidate> candidates = repository.findNearbyCandidates(condition, userId);

        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> popularityCounts = popularityRepository.findFavoriteCounts(
                candidates.stream()
                        .map(candidate -> candidate.attraction().id())
                        .toList()
        );

        if (popularityCounts.isEmpty()) {
            return candidates.stream()
                    .map(candidate -> toPopularAttraction(candidate, 0L))
                    .toList();
        }

        return candidates.stream()
                .map(candidate -> toPopularAttraction(
                        candidate,
                        popularityCounts.getOrDefault(candidate.attraction().id(), 0L)
                ))
                .sorted(Comparator
                        .comparingLong(PopularAttraction::popularityCount).reversed()
                        .thenComparingDouble(PopularAttraction::distanceMeters)
                        .thenComparing(
                                popular -> popular.attraction().title(),
                                Comparator.nullsLast(String::compareTo)
                        )
                        .thenComparing(
                                popular -> popular.attraction().id(),
                                Comparator.nullsLast(Long::compareTo)
                        ))
                .toList();
    }

    public boolean existsById(Long attractionId) {
        return repository.existsById(attractionId);
    }

    public AttractionStats findStats(Long attractionId, String userId) {
        return repository.findStats(attractionId, userId);
    }

    public void addFavorite(Long attractionId, String userId) {
        repository.addFavorite(attractionId, userId);
    }

    public boolean removeFavorite(Long attractionId, String userId) {
        return repository.removeFavorite(attractionId, userId);
    }

    public void upsertRating(Long attractionId, String userId, int rating) {
        repository.upsertRating(attractionId, userId, rating);
    }

    public boolean removeRating(Long attractionId, String userId) {
        return repository.removeRating(attractionId, userId);
    }

    public List<AttractionTag> findAllTags() {
        return repository.findAllTags();
    }

    public AttractionTag insertTag(String name) {
        return repository.insertTag(name);
    }

    public boolean updateTag(Long tagId, String name) {
        return repository.updateTag(tagId, name);
    }

    public boolean deleteTag(Long tagId) {
        return repository.deleteTag(tagId);
    }

    public boolean replaceTags(Long attractionId, List<Long> tagIds) {
        return repository.replaceTags(attractionId, tagIds);
    }

    private static PopularAttraction toPopularAttraction(NearbyAttractionCandidate candidate,
                                                         long popularityCount) {
        return new PopularAttraction(candidate.attraction(), candidate.distanceMeters(), popularityCount);
    }
}
