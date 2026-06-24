package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.ATTRACTION_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.AttractionPopularityDeltaCache;
import com.ssafy.enjoytrip.core.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.core.domain.PopularAttractionResult;
import com.ssafy.enjoytrip.core.domain.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import com.ssafy.enjoytrip.core.domain.vo.Address;
import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
import com.ssafy.enjoytrip.core.domain.vo.RatingStats;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttractionService {
    private final AttractionMapper attractionMapper;
    private final AttractionPopularityDeltaCache popularityDeltaCache;

    public List<PopularAttractionResult> findPopularNearbyAttractions(
            DistanceSearchCondition condition,
            Long memberId
    ) {
        List<NearbyAttractionCandidate> candidates = findNearbyAttractionCandidates(
                condition,
                memberId,
                false
        );

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

    public List<Attraction> searchAttractions(AttractionSearchCondition condition, Long memberId) {
        return executeAttractionSearch(condition, memberId);
    }

    public Attraction findAttractionDetail(Long attractionId, Long memberId) {
        AttractionSearchRecord record = attractionMapper.findDetailById(attractionId, memberId);
        if (record == null) {
            throw new CoreException(ATTRACTION_NOT_FOUND);
        }

        return toAttraction(record);
    }

    public List<NearbyAttractionCandidate> findNearbyCandidates(
            DistanceSearchCondition condition,
            Long memberId,
            boolean savedOnly
    ) {
        return findNearbyAttractionCandidates(condition, memberId, savedOnly);
    }

    public void addSave(Long attractionId, Long memberId) {
        if (attractionMapper.insertSave(attractionId, memberId) > 0) {
            popularityDeltaCache.recordSaveDelta(attractionId, 1L);
        }
    }

    public boolean removeSave(Long attractionId, Long memberId) {
        boolean deleted = attractionMapper.deleteSave(attractionId, memberId) > 0;
        if (deleted) {
            popularityDeltaCache.recordSaveDelta(attractionId, -1L);
        }
        return deleted;
    }

    @Transactional
    public void upsertRating(Long attractionId, Long memberId, int rating) {
        attractionMapper.upsertRating(attractionId, memberId, rating);
        attractionMapper.refreshPopularityRatingStats(attractionId);
    }

    @Transactional
    public boolean removeRating(Long attractionId, Long memberId) {
        boolean deleted = attractionMapper.deleteRating(attractionId, memberId) > 0;
        if (deleted) {
            attractionMapper.refreshPopularityRatingStats(attractionId);
        }

        return deleted;
    }

    private List<Attraction> executeAttractionSearch(AttractionSearchCondition condition, Long memberId) {
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
                memberId
        );
        return records.stream()
                .map(this::toAttraction)
                .toList();
    }

    private List<NearbyAttractionCandidate> findNearbyAttractionCandidates(
            DistanceSearchCondition condition,
            Long memberId,
            boolean savedOnly
    ) {
        List<AttractionSearchRecord> records = attractionMapper.findNearby(
                condition.longitude(),
                condition.latitude(),
                condition.radiusMeters(),
                condition.limit(),
                savedOnly,
                memberId
        );
        return records.stream()
                .map(record -> new NearbyAttractionCandidate(
                        toAttraction(record),
                        record.distanceMeters() == null ? 0.0 : record.distanceMeters()
                ))
                .toList();
    }

    private Attraction toAttraction(AttractionSearchRecord record) {
        Coordinate location = (record.latitude() != null && record.longitude() != null)
                ? new Coordinate(record.latitude(), record.longitude())
                : null;
        return new Attraction(
                record.id(),
                record.title(),
                new Address(record.addr1(), record.addr2(), record.zipcode()),
                record.tel(),
                record.firstImage(),
                record.firstImage2(),
                record.readCount(),
                record.sidoCode(),
                record.gugunCode(),
                location,
                record.mlevel(),
                record.contentTypeId(),
                record.overview(),
                record.saveCount(),
                new RatingStats(record.ratingAverage(), record.ratingCount()),
                record.saved(),
                record.myRating()
        );
    }

    public List<NearbyAttractionCandidate> searchMapPlaces(
            String keyword,
            String escapedKeyword,
            double longitude,
            double latitude,
            Double radiusMeters,
            Integer limit,
            Long viewerMemberId
    ) {
        long t0 = System.nanoTime();
        List<AttractionSearchRecord> records = attractionMapper.searchMapPlaces(
                keyword,
                escapedKeyword,
                longitude,
                latitude,
                radiusMeters,
                limit,
                viewerMemberId
        );
        int rows = records.size();
        log.info(
                "map-search places keyword={} radius={} rows={} tookMs={}",
                keyword,
                radiusMeters,
                rows,
                (System.nanoTime() - t0) / 1_000_000.0
        );

        return records.stream()
                .map(record -> new NearbyAttractionCandidate(
                        toAttraction(record),
                        record.distanceMeters() == null ? 0.0 : record.distanceMeters()
                ))
                .toList();
    }
}
