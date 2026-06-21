package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.AttractionStats;
import com.ssafy.enjoytrip.core.domain.AttractionTag;
import com.ssafy.enjoytrip.core.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.domain.PopularAttraction;
import com.ssafy.enjoytrip.core.domain.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.core.domain.query.NearbySearchCondition;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionTagRecord;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttractionService {
    private final AttractionMapper attractionMapper;
    private final AttractionStatsReader attractionStatsReader;
    private final AttractionPopularityDeltaCache popularityDeltaCache;

    public List<PopularAttraction> findPopularNearbyAttractions(NearbySearchCondition condition,
                                                                String userId) {
        List<NearbyAttractionCandidate> candidates = findNearbyAttractionCandidates(condition, userId);

        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> popularityCounts = findPopularityFavoriteCounts(candidates);

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

    private Map<Long, Long> findPopularityFavoriteCounts(List<NearbyAttractionCandidate> candidates) {
        return attractionMapper.findPopularityFavoriteCounts(candidates.stream()
                        .map(candidate -> candidate.attraction().id())
                        .toList())
                .stream()
                .collect(Collectors.toMap(
                        AttractionCountRecord::attractionId,
                        record -> Long.valueOf(record.count())
                ));
    }

    public void addFavorite(Long attractionId, String userId) {
        if (userId == null) {
            return;
        }
        if (attractionMapper.insertFavorite(attractionId, userId) > 0) {
            popularityDeltaCache.recordFavoriteDelta(attractionId, 1L);
        }
    }

    public boolean removeFavorite(Long attractionId, String userId) {
        boolean deleted = attractionMapper.deleteFavorite(attractionId, userId) > 0;
        if (deleted) {
            popularityDeltaCache.recordFavoriteDelta(attractionId, -1L);
        }
        return deleted;
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

    public AttractionTag createTagOrThrow(String name) {
        requireTagNameAvailable(null, name);
        return insertTag(name);
    }

    public AttractionTag insertTag(String name) {
        AttractionTagRecord record = attractionMapper.insertTag(name);
        return new AttractionTag(record.id(), record.name());
    }

    public void updateTagOrThrow(Long tagId, String name) {
        requireTagNameAvailable(tagId, name);
        if (!updateTag(tagId, name)) {
            throw new CoreException(TAG_NOT_FOUND);
        }
    }

    public boolean updateTag(Long tagId, String name) {
        return attractionMapper.updateTag(tagId, name) > 0;
    }

    public void deleteTagOrThrow(Long tagId) {
        if (!deleteTag(tagId)) {
            throw new CoreException(TAG_NOT_FOUND);
        }
    }

    public boolean deleteTag(Long tagId) {
        return attractionMapper.deleteTag(tagId) > 0;
    }

    public void replaceTagsOrThrow(Long attractionId, List<Long> tagIds) {
        if (!replaceTags(attractionId, tagIds)) {
            throw new CoreException(TAG_NOT_FOUND);
        }
    }

    @Transactional
    public boolean replaceTags(Long attractionId, List<Long> tagIds) {
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

    private void requireTagNameAvailable(Long currentId, String name) {
        boolean exists = findAllTags().stream()
                .anyMatch(tag -> tag.name().equals(name) && !tag.id().equals(currentId));
        if (exists) {
            throw new CoreException(TAG_ALREADY_EXISTS);
        }
    }


    private List<Attraction> executeAttractionSearch(AttractionSearchCondition condition, String userId) {
        List<Attraction> attractions = attractionMapper.search(
                        condition.contentTypeId(),
                        condition.keyword(),
                        condition.aroundSearch() ? null : condition.sidoCode(),
                        condition.aroundSearch() ? null : condition.gugunCode(),
                        condition.longitude(),
                        condition.latitude(),
                        condition.radiusMeters(),
                        condition.aroundSearch(),
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
                .collect(Collectors.toMap(
                        AttractionSearchRecord::id,
                        AttractionSearchRecord::distanceMeters
                ));

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
        Map<Long, AttractionStats> stats = attractionStatsReader.findStatsByAttractionId(
                attractions.stream().map(Attraction::id).toList(),
                userId
        );
        return attractions.stream()
                .map(attraction -> enrich(attraction, stats.get(attraction.id())))
                .toList();
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

}
