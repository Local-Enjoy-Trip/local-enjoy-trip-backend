package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.AttractionTag;
import com.ssafy.enjoytrip.core.domain.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.core.domain.query.NearbySearchCondition;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionTagRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttractionService {
    private final AttractionMapper attractionMapper;
    private final AttractionPopularityDeltaCache popularityDeltaCache;

    public List<PopularAttractionResult> findPopularNearbyAttractions(
            NearbySearchCondition condition,
            String userId
    ) {
        List<NearbyAttractionCandidate> candidates = findNearbyAttractionCandidates(condition, userId, false);

        if (candidates.isEmpty()) {
            return List.of();
        }

        return candidates.stream()
                .map(candidate -> new PopularAttractionResult(
                        candidate.attraction(),
                        candidate.distanceMeters(),
                        candidate.attraction().saveCount()
                ))
                .sorted(Comparator
                        .comparingLong(PopularAttractionResult::popularityCount).reversed()
                        .thenComparingDouble(PopularAttractionResult::distanceMeters)
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
        return executeAttractionSearch(condition, null);
    }

    public List<Attraction> searchAttractions(AttractionSearchCondition condition, String userId) {
        return executeAttractionSearch(condition, userId);
    }

    public List<NearbyAttractionCandidate> findNearbyCandidates(
            NearbySearchCondition condition,
            String userId,
            boolean savedOnly
    ) {
        return findNearbyAttractionCandidates(condition, userId, savedOnly);
    }

    public void addSave(Long attractionId, String userId) {
        if (attractionMapper.insertSave(attractionId, userId) > 0) {
            popularityDeltaCache.recordSaveDelta(attractionId, 1L);
        }
    }

    public boolean removeSave(Long attractionId, String userId) {
        boolean deleted = attractionMapper.deleteSave(attractionId, userId) > 0;
        if (deleted) {
            popularityDeltaCache.recordSaveDelta(attractionId, -1L);
        }
        return deleted;
    }

    @Transactional
    public void upsertRating(Long attractionId, String userId, int rating) {
        attractionMapper.upsertRating(attractionId, userId, rating);
        attractionMapper.refreshPopularityRatingStats(attractionId);
    }

    @Transactional
    public boolean removeRating(Long attractionId, String userId) {
        boolean deleted = attractionMapper.deleteRating(attractionId, userId) > 0;
        if (deleted) {
            attractionMapper.refreshPopularityRatingStats(attractionId);
        }

        return deleted;
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
        List<AttractionSearchRecord> records = attractionMapper.search(
                condition.contentTypeId(),
                condition.keyword(),
                condition.aroundSearch() ? null : condition.sidoCode(),
                condition.aroundSearch() ? null : condition.gugunCode(),
                condition.longitude(),
                condition.latitude(),
                condition.radiusMeters(),
                condition.aroundSearch(),
                200,
                userId
        );
        return toAttractions(records);
    }

    private List<NearbyAttractionCandidate> findNearbyAttractionCandidates(
            NearbySearchCondition condition,
            String userId,
            boolean savedOnly
    ) {
        List<AttractionSearchRecord> records = attractionMapper.findNearby(
                condition.longitude(),
                condition.latitude(),
                condition.radiusMeters(),
                condition.limit(),
                savedOnly,
                userId
        );
        return toNearbyCandidates(records);
    }

    private List<Attraction> toAttractions(List<AttractionSearchRecord> records) {
        List<Attraction> attractions = new ArrayList<>();
        List<AttractionSearchRecord> attractionRows = new ArrayList<>();
        Long currentAttractionId = null;

        for (AttractionSearchRecord record : records) {
            if (currentAttractionId != null && !Objects.equals(currentAttractionId, record.id())) {
                attractions.add(toAttraction(attractionRows));
                attractionRows.clear();
            }
            currentAttractionId = record.id();
            attractionRows.add(record);
        }

        if (!attractionRows.isEmpty()) {
            attractions.add(toAttraction(attractionRows));
        }

        return attractions;
    }

    private List<NearbyAttractionCandidate> toNearbyCandidates(List<AttractionSearchRecord> records) {
        List<NearbyAttractionCandidate> candidates = new ArrayList<>();
        List<AttractionSearchRecord> attractionRows = new ArrayList<>();
        Long currentAttractionId = null;

        for (AttractionSearchRecord record : records) {
            if (currentAttractionId != null && !Objects.equals(currentAttractionId, record.id())) {
                candidates.add(toNearbyCandidate(attractionRows));
                attractionRows.clear();
            }
            currentAttractionId = record.id();
            attractionRows.add(record);
        }

        if (!attractionRows.isEmpty()) {
            candidates.add(toNearbyCandidate(attractionRows));
        }

        return candidates;
    }

    private NearbyAttractionCandidate toNearbyCandidate(List<AttractionSearchRecord> attractionRows) {
        AttractionSearchRecord first = attractionRows.get(0);
        return new NearbyAttractionCandidate(toAttraction(attractionRows), first.distanceMeters() == null ? 0.0 : first.distanceMeters());
    }

    private Attraction toAttraction(List<AttractionSearchRecord> attractionRows) {
        AttractionSearchRecord first = attractionRows.get(0);
        return new Attraction(
                first.id(),
                first.title(),
                first.addr1(),
                first.addr2(),
                first.zipcode(),
                first.tel(),
                first.firstImage(),
                first.firstImage2(),
                first.readCount(),
                first.sidoCode(),
                first.gugunCode(),
                first.latitude(),
                first.longitude(),
                first.mlevel(),
                first.contentTypeId(),
                first.overview(),
                first.saveCount(),
                first.ratingAverage(),
                first.ratingCount(),
                toTags(attractionRows),
                first.saved(),
                first.myRating()
        );
    }

    private List<AttractionTag> toTags(List<AttractionSearchRecord> attractionRows) {
        return attractionRows.stream()
                .filter(row -> row.tagId() != null)
                .map(row -> new AttractionTag(row.tagId(), row.tagName()))
                .toList();
    }

}
