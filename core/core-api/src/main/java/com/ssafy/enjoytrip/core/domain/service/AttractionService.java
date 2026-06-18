package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.AttractionStats;
import com.ssafy.enjoytrip.core.domain.AttractionTag;
import com.ssafy.enjoytrip.core.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.core.domain.PopularAttraction;
import com.ssafy.enjoytrip.core.domain.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.core.domain.query.NearbySearchCondition;
import com.ssafy.enjoytrip.external.ClickHouseAttractionPopularityClient;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionAverageRatingRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRatingRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionTagRecord;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttractionService {
    private final ClickHouseAttractionPopularityClient popularityClient;
    private final AttractionMapper attractionMapper;

    public List<PopularAttraction> findPopularNearbyAttractions(NearbySearchCondition condition,
                                                                String userId) {
        List<NearbyAttractionCandidate> candidates = findNearbyAttractionCandidates(condition, userId);

        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> popularityCounts = popularityClient.findFavoriteCounts(
                candidates.stream()
                        .map(candidate -> candidate.attraction().id())
                        .toList()
        );

        if (popularityCounts.isEmpty()) {
            return candidates.stream()
                    .map(candidate -> new PopularAttraction(candidate.attraction(), candidate.distanceMeters(), 0L))
                    .toList();
        }

        return candidates.stream()
                .map(candidate -> new PopularAttraction(
                        candidate.attraction(),
                        candidate.distanceMeters(),
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

    public List<Attraction> searchAttractions(AttractionSearchCondition condition) {
        return executeAttractionSearch(condition, "");
    }

    public List<Attraction> searchAttractions(AttractionSearchCondition condition, String userId) {
        return executeAttractionSearch(condition, userId);
    }

    public List<NearbyAttractionCandidate> findNearbyCandidates(NearbySearchCondition condition,
                                                                String userId) {
        return findNearbyAttractionCandidates(condition, userId);
    }

    public boolean existsById(Long attractionId) {
        return attractionId != null && attractionMapper.existsById(attractionId) > 0;
    }

    public AttractionStats findStats(Long attractionId, String userId) {
        return statsFor(List.of(attractionId), userId)
                .getOrDefault(attractionId, new AttractionStats(attractionId, 0, 0.0, 0, List.of(), false, null));
    }

    public void addFavorite(Long attractionId, String userId) {
        if (blankToNull(userId) == null) {
            return;
        }
        attractionMapper.insertFavorite(attractionId, userId);
    }

    public boolean removeFavorite(Long attractionId, String userId) {
        return attractionMapper.deleteFavorite(attractionId, userId) > 0;
    }

    public void upsertRating(Long attractionId, String userId, int rating) {
        attractionMapper.upsertRating(attractionId, userId, rating);
    }

    public boolean removeRating(Long attractionId, String userId) {
        return attractionMapper.deleteRating(attractionId, userId) > 0;
    }

    public List<AttractionTag> findAllTags() {
        return attractionMapper.findAllTags().stream()
                .map(record -> new AttractionTag(record.id(), record.name()))
                .toList();
    }

    public AttractionTag insertTag(String name) {
        AttractionTagRecord record = attractionMapper.insertTag(name);
        return new AttractionTag(record.id(), record.name());
    }

    public boolean updateTag(Long tagId, String name) {
        return attractionMapper.updateTag(tagId, name) > 0;
    }

    public boolean deleteTag(Long tagId) {
        return attractionMapper.deleteTag(tagId) > 0;
    }

    @Transactional
    public boolean replaceTags(Long attractionId, List<Long> tagIds) {
        if (attractionId == null || attractionMapper.existsById(attractionId) <= 0) {
            return false;
        }

        List<Long> normalized = tagIds.stream().distinct().toList();
        if (!normalized.isEmpty() && attractionMapper.countTagsByIds(normalized) != normalized.size()) {
            return false;
        }

        attractionMapper.deleteTagMappings(attractionId);
        for (Long tagId : normalized) {
            attractionMapper.insertTagMapping(attractionId, tagId);
        }

        return true;
    }

    private List<Attraction> executeAttractionSearch(AttractionSearchCondition condition, String userId) {
        Double longitude = parseDouble(condition.mapX());
        Double latitude = parseDouble(condition.mapY());
        boolean aroundSearch = longitude != null && latitude != null;
        Double radiusMeters = aroundSearch ? parseRadius(condition.radius()) : null;

        List<Attraction> attractions = attractionMapper.search(
                        blankToNull(condition.contentTypeId()),
                        blankToNull(condition.keyword()),
                        aroundSearch ? null : parseInteger(condition.sidoCode()),
                        aroundSearch ? null : parseInteger(condition.gugunCode()),
                        longitude,
                        latitude,
                        radiusMeters,
                        aroundSearch,
                        200
                ).stream()
                .map(record -> new Attraction(
                        record.id(),
                        record.title(),
                        record.addr1(),
                        record.addr2(),
                        record.zipcode(),
                        record.tel(),
                        record.firstImage(),
                        record.firstImage2(),
                        record.readCount(),
                        record.sidoCode(),
                        record.gugunCode(),
                        record.latitude(),
                        record.longitude(),
                        record.mlevel(),
                        record.contentTypeId(),
                        record.overview(),
                        0,
                        0.0,
                        0,
                        List.of(),
                        false,
                        null
                ))
                .toList();
        return enrich(attractions, userId);
    }

    private List<NearbyAttractionCandidate> findNearbyAttractionCandidates(NearbySearchCondition condition,
                                                                           String userId) {
        List<AttractionSearchRecord> records = attractionMapper.findNearby(
                condition.longitude(),
                condition.latitude(),
                condition.radiusMeters(),
                condition.limit()
        );
        List<Attraction> enriched = enrich(records.stream()
                .map(record -> new Attraction(
                        record.id(),
                        record.title(),
                        record.addr1(),
                        record.addr2(),
                        record.zipcode(),
                        record.tel(),
                        record.firstImage(),
                        record.firstImage2(),
                        record.readCount(),
                        record.sidoCode(),
                        record.gugunCode(),
                        record.latitude(),
                        record.longitude(),
                        record.mlevel(),
                        record.contentTypeId(),
                        record.overview(),
                        0,
                        0.0,
                        0,
                        List.of(),
                        false,
                        null
                ))
                .toList(), userId);
        Map<Long, Double> distanceByAttractionId = records.stream()
                .collect(Collectors.toMap(AttractionSearchRecord::id, AttractionSearchRecord::distanceMeters));

        return enriched.stream()
                .map(attraction -> new NearbyAttractionCandidate(
                        attraction,
                        distanceByAttractionId.getOrDefault(attraction.id(), 0.0)
                ))
                .toList();
    }

    private List<Attraction> enrich(List<Attraction> attractions, String userId) {
        if (attractions.isEmpty()) {
            return attractions;
        }
        Map<Long, AttractionStats> stats = statsFor(
                attractions.stream().map(Attraction::id).toList(),
                userId
        );
        return attractions.stream()
                .map(attraction -> enrich(attraction, stats.get(attraction.id())))
                .toList();
    }

    private Map<Long, AttractionStats> statsFor(List<Long> attractionIds, String userId) {
        if (attractionIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> favoriteCounts = attractionMapper.findFavoriteCounts(attractionIds).stream()
                .collect(Collectors.toMap(AttractionCountRecord::attractionId, AttractionCountRecord::count));
        Map<Long, AttractionAverageRatingRecord> ratingStats = attractionMapper.findRatingStats(attractionIds).stream()
                .collect(Collectors.toMap(AttractionAverageRatingRecord::attractionId, record -> record));
        Set<Long> favoritedIds = new HashSet<>();
        Map<Long, Integer> myRatings = Map.of();
        if (blankToNull(userId) != null) {
            favoritedIds = new HashSet<>(attractionMapper.findFavoritedIds(attractionIds, userId));
            myRatings = attractionMapper.findMyRatings(attractionIds, userId).stream()
                    .collect(Collectors.toMap(AttractionRatingRecord::attractionId, AttractionRatingRecord::rating));
        }

        Map<Long, AttractionStats> result = new HashMap<>();
        for (Long attractionId : attractionIds) {
            AttractionAverageRatingRecord rating = ratingStats.get(attractionId);
            result.put(attractionId, new AttractionStats(
                    attractionId,
                    favoriteCounts.getOrDefault(attractionId, 0),
                    rating == null ? 0.0 : rating.average(),
                    rating == null ? 0 : rating.count(),
                    attractionMapper.findTagsByAttractionId(attractionId).stream()
                            .map(record -> new AttractionTag(record.id(), record.name()))
                            .toList(),
                    favoritedIds.contains(attractionId),
                    myRatings.get(attractionId)
            ));
        }
        return result;
    }

    private static Attraction enrich(Attraction attraction, AttractionStats stats) {
        if (stats == null) {
            return attraction;
        }
        return new Attraction(
                attraction.id(),
                attraction.title(),
                attraction.addr1(),
                attraction.addr2(),
                attraction.zipcode(),
                attraction.tel(),
                attraction.firstImage(),
                attraction.firstImage2(),
                attraction.readcount(),
                attraction.sidoCode(),
                attraction.gugunCode(),
                attraction.latitude(),
                attraction.longitude(),
                attraction.mlevel(),
                attraction.contentTypeId(),
                attraction.overview(),
                stats.favoriteCount(),
                stats.ratingAverage(),
                stats.ratingCount(),
                stats.tags(),
                stats.favorited(),
                stats.myRating()
        );
    }

    private static Integer parseInteger(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Double parseDouble(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static double parseRadius(String value) {
        Double parsed = parseDouble(value);
        if (parsed == null || parsed <= 0) {
            return 3000.0;
        }
        return Math.min(parsed, 20000.0);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
