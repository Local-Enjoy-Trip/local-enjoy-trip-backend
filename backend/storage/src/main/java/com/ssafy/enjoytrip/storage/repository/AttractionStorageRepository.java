package com.ssafy.enjoytrip.storage.repository;

import static com.ssafy.enjoytrip.storage.jooq.tables.AttractionFavorites.ATTRACTION_FAVORITES;
import static com.ssafy.enjoytrip.storage.jooq.tables.AttractionRatings.ATTRACTION_RATINGS;
import static com.ssafy.enjoytrip.storage.jooq.tables.AttractionTagMappings.ATTRACTION_TAG_MAPPINGS;
import static com.ssafy.enjoytrip.storage.jooq.tables.AttractionTags.ATTRACTION_TAGS;
import static com.ssafy.enjoytrip.storage.jooq.tables.Attractions.ATTRACTIONS;
import static org.jooq.impl.DSL.avg;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.noCondition;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.domain.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.AttractionStats;
import com.ssafy.enjoytrip.domain.AttractionTag;
import com.ssafy.enjoytrip.repository.AttractionRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class AttractionStorageRepository implements AttractionRepository {
    private final DSLContext dslContext;

    @Override
    public List<Attraction> search(AttractionSearchCondition condition) {
        return search(condition, "");
    }

    @Override
    public List<Attraction> search(AttractionSearchCondition condition, String userId) {
        Double longitude = parseDouble(condition.mapX());
        Double latitude = parseDouble(condition.mapY());
        boolean aroundSearch = longitude != null && latitude != null;

        var query = dslContext.select(
                        ATTRACTIONS.ID,
                        ATTRACTIONS.TITLE,
                        ATTRACTIONS.ADDR1,
                        ATTRACTIONS.ADDR2,
                        ATTRACTIONS.ZIPCODE,
                        ATTRACTIONS.TEL,
                        ATTRACTIONS.FIRST_IMAGE.as("firstImage"),
                        ATTRACTIONS.FIRST_IMAGE2.as("firstImage2"),
                        ATTRACTIONS.READ_COUNT.as("readcount"),
                        ATTRACTIONS.SIDO_CODE.as("sidoCode"),
                        ATTRACTIONS.GUGUN_CODE.as("gugunCode"),
                        field("ST_Y({0})", Double.class, ATTRACTIONS.LOCATION).as("latitude"),
                        field("ST_X({0})", Double.class, ATTRACTIONS.LOCATION).as("longitude"),
                        ATTRACTIONS.MLEVEL,
                        ATTRACTIONS.CONTENT_TYPE_ID.as("contentTypeId"),
                        ATTRACTIONS.OVERVIEW
                )
                .from(ATTRACTIONS)
                .where(searchCondition(condition, longitude, latitude, aroundSearch));

        if (aroundSearch) {
            return enrich(query
                    .orderBy(field("{0} <-> ST_SetSRID(ST_MakePoint({1}, {2}), 4326)",
                                    ATTRACTIONS.LOCATION, longitude, latitude),
                            ATTRACTIONS.TITLE.asc())
                    .limit(200)
                    .fetch(AttractionStorageRepository::toAttraction), userId);
        }

        return enrich(query
                .orderBy(ATTRACTIONS.READ_COUNT.desc().nullsLast(), ATTRACTIONS.TITLE.asc())
                .limit(200)
                .fetch(AttractionStorageRepository::toAttraction), userId);
    }

    @Override
    public boolean existsById(Long attractionId) {
        return attractionId != null && dslContext.fetchExists(ATTRACTIONS, ATTRACTIONS.ID.eq(attractionId));
    }

    @Override
    public AttractionStats findStats(Long attractionId, String userId) {
        return statsFor(List.of(attractionId), userId).getOrDefault(attractionId, emptyStats(attractionId));
    }

    @Override
    @Transactional
    public void addFavorite(Long attractionId, String userId) {
        if (blankToNull(userId) == null) {
            return;
        }
        dslContext.insertInto(ATTRACTION_FAVORITES)
                .set(ATTRACTION_FAVORITES.ATTRACTION_ID, attractionId)
                .set(ATTRACTION_FAVORITES.USER_ID, userId)
                .onConflict(ATTRACTION_FAVORITES.ATTRACTION_ID, ATTRACTION_FAVORITES.USER_ID)
                .doNothing()
                .execute();
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long attractionId, String userId) {
        return dslContext.deleteFrom(ATTRACTION_FAVORITES)
                .where(ATTRACTION_FAVORITES.ATTRACTION_ID.eq(attractionId))
                .and(ATTRACTION_FAVORITES.USER_ID.eq(userId))
                .execute() > 0;
    }

    @Override
    @Transactional
    public void upsertRating(Long attractionId, String userId, int rating) {
        dslContext.insertInto(ATTRACTION_RATINGS)
                .set(ATTRACTION_RATINGS.ATTRACTION_ID, attractionId)
                .set(ATTRACTION_RATINGS.USER_ID, userId)
                .set(ATTRACTION_RATINGS.RATING, rating)
                .onConflict(ATTRACTION_RATINGS.ATTRACTION_ID, ATTRACTION_RATINGS.USER_ID)
                .doUpdate()
                .set(ATTRACTION_RATINGS.RATING, rating)
                .set(ATTRACTION_RATINGS.UPDATED_AT, field("current_timestamp", java.time.LocalDateTime.class))
                .execute();
    }

    @Override
    @Transactional
    public boolean removeRating(Long attractionId, String userId) {
        return dslContext.deleteFrom(ATTRACTION_RATINGS)
                .where(ATTRACTION_RATINGS.ATTRACTION_ID.eq(attractionId))
                .and(ATTRACTION_RATINGS.USER_ID.eq(userId))
                .execute() > 0;
    }

    @Override
    public List<AttractionTag> findAllTags() {
        return dslContext.select(ATTRACTION_TAGS.ID, ATTRACTION_TAGS.NAME)
                .from(ATTRACTION_TAGS)
                .orderBy(ATTRACTION_TAGS.NAME.asc())
                .fetch(record -> new AttractionTag(record.get(ATTRACTION_TAGS.ID), record.get(ATTRACTION_TAGS.NAME)));
    }

    @Override
    @Transactional
    public AttractionTag insertTag(String name) {
        return dslContext.insertInto(ATTRACTION_TAGS)
                .set(ATTRACTION_TAGS.NAME, name)
                .returning(ATTRACTION_TAGS.ID, ATTRACTION_TAGS.NAME)
                .fetchOne(record -> new AttractionTag(record.get(ATTRACTION_TAGS.ID), record.get(ATTRACTION_TAGS.NAME)));
    }

    @Override
    @Transactional
    public boolean updateTag(Long tagId, String name) {
        return dslContext.update(ATTRACTION_TAGS)
                .set(ATTRACTION_TAGS.NAME, name)
                .where(ATTRACTION_TAGS.ID.eq(tagId))
                .execute() > 0;
    }

    @Override
    @Transactional
    public boolean deleteTag(Long tagId) {
        return dslContext.deleteFrom(ATTRACTION_TAGS)
                .where(ATTRACTION_TAGS.ID.eq(tagId))
                .execute() > 0;
    }

    @Override
    @Transactional
    public boolean replaceTags(Long attractionId, List<Long> tagIds) {
        if (!existsById(attractionId)) {
            return false;
        }
        List<Long> normalized = tagIds.stream().distinct().toList();
        if (!normalized.isEmpty()) {
            int existing = dslContext.selectCount()
                    .from(ATTRACTION_TAGS)
                    .where(ATTRACTION_TAGS.ID.in(normalized))
                    .fetchOne(0, int.class);
            if (existing != normalized.size()) {
                return false;
            }
        }
        dslContext.deleteFrom(ATTRACTION_TAG_MAPPINGS)
                .where(ATTRACTION_TAG_MAPPINGS.ATTRACTION_ID.eq(attractionId))
                .execute();
        for (Long tagId : normalized) {
            dslContext.insertInto(ATTRACTION_TAG_MAPPINGS)
                    .set(ATTRACTION_TAG_MAPPINGS.ATTRACTION_ID, attractionId)
                    .set(ATTRACTION_TAG_MAPPINGS.TAG_ID, tagId)
                    .execute();
        }
        return true;
    }

    private Condition searchCondition(AttractionSearchCondition condition, Double longitude, Double latitude, boolean aroundSearch) {
        Condition searchCondition = contentTypeCondition(condition.contentTypeId())
                .and(keywordCondition(condition.keyword()));

        if (aroundSearch) {
            double parsedRadius = parseRadius(condition.radius());
            return searchCondition
                    .and(ATTRACTIONS.LOCATION.isNotNull())
                    .and("ST_DWithin({0}::geography, ST_SetSRID(ST_MakePoint({1}, {2}), 4326)::geography, {3})",
                            ATTRACTIONS.LOCATION, longitude, latitude, parsedRadius);
        }

        return searchCondition
                .and(sidoCodeCondition(condition.sidoCode()))
                .and(gugunCodeCondition(condition.gugunCode()));
    }

    private Condition sidoCodeCondition(String sidoCode) {
        Integer parsed = parseInteger(sidoCode);
        return parsed == null ? noCondition() : ATTRACTIONS.SIDO_CODE.eq(parsed);
    }

    private Condition gugunCodeCondition(String gugunCode) {
        Integer parsed = parseInteger(gugunCode);
        return parsed == null ? noCondition() : ATTRACTIONS.GUGUN_CODE.eq(parsed);
    }

    private Condition contentTypeCondition(String contentTypeId) {
        String normalized = blankToNull(contentTypeId);
        return normalized == null ? noCondition() : ATTRACTIONS.CONTENT_TYPE_ID.eq(normalized);
    }

    private Condition keywordCondition(String keyword) {
        String normalized = blankToNull(keyword);
        if (normalized == null) {
            return noCondition();
        }
        String pattern = "%" + normalized + "%";
        return ATTRACTIONS.TITLE.likeIgnoreCase(pattern)
                .or(coalesce(ATTRACTIONS.ADDR1, "").likeIgnoreCase(pattern))
                .or(coalesce(ATTRACTIONS.ADDR2, "").likeIgnoreCase(pattern))
                .or(coalesce(ATTRACTIONS.OVERVIEW, "").likeIgnoreCase(pattern));
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
            return 3_000.0;
        }
        return parsed;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private List<Attraction> enrich(List<Attraction> attractions, String userId) {
        if (attractions.isEmpty()) {
            return attractions;
        }
        Map<Long, AttractionStats> statsByAttraction = statsFor(attractions.stream().map(Attraction::id).toList(), userId);
        return attractions.stream()
                .map(attraction -> {
                    AttractionStats stats = statsByAttraction.getOrDefault(attraction.id(), emptyStats(attraction.id()));
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
                })
                .toList();
    }

    private Map<Long, AttractionStats> statsFor(List<Long> attractionIds, String userId) {
        Map<Long, Integer> favoriteCounts = favoriteCounts(attractionIds);
        Map<Long, RatingAggregate> ratingAggregates = ratingAggregates(attractionIds);
        Map<Long, List<AttractionTag>> tags = tagsByAttraction(attractionIds);
        Set<Long> userFavorites = userFavorites(attractionIds, userId);
        Map<Long, Integer> userRatings = userRatings(attractionIds, userId);

        Map<Long, AttractionStats> result = new HashMap<>();
        for (Long attractionId : attractionIds) {
            RatingAggregate rating = ratingAggregates.getOrDefault(attractionId, new RatingAggregate(0.0, 0));
            result.put(attractionId, new AttractionStats(
                    attractionId,
                    favoriteCounts.getOrDefault(attractionId, 0),
                    rating.average(),
                    rating.count(),
                    tags.getOrDefault(attractionId, List.of()),
                    userFavorites.contains(attractionId),
                    userRatings.get(attractionId)
            ));
        }
        return result;
    }

    private Map<Long, Integer> favoriteCounts(List<Long> attractionIds) {
        return dslContext.select(ATTRACTION_FAVORITES.ATTRACTION_ID, count().as("favoriteCount"))
                .from(ATTRACTION_FAVORITES)
                .where(ATTRACTION_FAVORITES.ATTRACTION_ID.in(attractionIds))
                .groupBy(ATTRACTION_FAVORITES.ATTRACTION_ID)
                .fetchMap(ATTRACTION_FAVORITES.ATTRACTION_ID, field("favoriteCount", Integer.class));
    }

    private Map<Long, RatingAggregate> ratingAggregates(List<Long> attractionIds) {
        return dslContext.select(
                        ATTRACTION_RATINGS.ATTRACTION_ID,
                        avg(ATTRACTION_RATINGS.RATING).as("ratingAverage"),
                        count().as("ratingCount")
                )
                .from(ATTRACTION_RATINGS)
                .where(ATTRACTION_RATINGS.ATTRACTION_ID.in(attractionIds))
                .groupBy(ATTRACTION_RATINGS.ATTRACTION_ID)
                .fetchMap(
                        ATTRACTION_RATINGS.ATTRACTION_ID,
                        record -> new RatingAggregate(
                                roundedAverage(record.get("ratingAverage", BigDecimal.class)),
                                record.get("ratingCount", Integer.class)
                        )
                );
    }

    private Map<Long, List<AttractionTag>> tagsByAttraction(List<Long> attractionIds) {
        return dslContext.select(
                        ATTRACTION_TAG_MAPPINGS.ATTRACTION_ID,
                        ATTRACTION_TAGS.ID,
                        ATTRACTION_TAGS.NAME
                )
                .from(ATTRACTION_TAG_MAPPINGS)
                .join(ATTRACTION_TAGS).on(ATTRACTION_TAGS.ID.eq(ATTRACTION_TAG_MAPPINGS.TAG_ID))
                .where(ATTRACTION_TAG_MAPPINGS.ATTRACTION_ID.in(attractionIds))
                .orderBy(ATTRACTION_TAG_MAPPINGS.ATTRACTION_ID.asc(), ATTRACTION_TAGS.NAME.asc())
                .fetchGroups(
                        ATTRACTION_TAG_MAPPINGS.ATTRACTION_ID,
                        record -> new AttractionTag(record.get(ATTRACTION_TAGS.ID), record.get(ATTRACTION_TAGS.NAME))
                );
    }

    private Set<Long> userFavorites(List<Long> attractionIds, String userId) {
        if (blankToNull(userId) == null) {
            return Set.of();
        }
        return new HashSet<>(dslContext.select(ATTRACTION_FAVORITES.ATTRACTION_ID)
                .from(ATTRACTION_FAVORITES)
                .where(ATTRACTION_FAVORITES.ATTRACTION_ID.in(attractionIds))
                .and(ATTRACTION_FAVORITES.USER_ID.eq(userId))
                .fetch(ATTRACTION_FAVORITES.ATTRACTION_ID));
    }

    private Map<Long, Integer> userRatings(List<Long> attractionIds, String userId) {
        if (blankToNull(userId) == null) {
            return Map.of();
        }
        return dslContext.select(ATTRACTION_RATINGS.ATTRACTION_ID, ATTRACTION_RATINGS.RATING)
                .from(ATTRACTION_RATINGS)
                .where(ATTRACTION_RATINGS.ATTRACTION_ID.in(attractionIds))
                .and(ATTRACTION_RATINGS.USER_ID.eq(userId))
                .fetchMap(ATTRACTION_RATINGS.ATTRACTION_ID, ATTRACTION_RATINGS.RATING);
    }

    private static AttractionStats emptyStats(Long attractionId) {
        return new AttractionStats(attractionId, 0, 0.0, 0, List.of(), false, null);
    }

    private static double roundedAverage(BigDecimal value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value.doubleValue() * 10.0) / 10.0;
    }

    private static Attraction toAttraction(org.jooq.Record record) {
        return new Attraction(
                record.get(ATTRACTIONS.ID),
                record.get(ATTRACTIONS.TITLE),
                record.get(ATTRACTIONS.ADDR1),
                record.get(ATTRACTIONS.ADDR2),
                record.get(ATTRACTIONS.ZIPCODE),
                record.get(ATTRACTIONS.TEL),
                record.get("firstImage", String.class),
                record.get("firstImage2", String.class),
                record.get("readcount", Integer.class),
                record.get("sidoCode", Integer.class),
                record.get("gugunCode", Integer.class),
                record.get("latitude", Double.class),
                record.get("longitude", Double.class),
                record.get(ATTRACTIONS.MLEVEL),
                record.get("contentTypeId", String.class),
                record.get(ATTRACTIONS.OVERVIEW),
                0,
                0.0,
                0,
                List.of(),
                false,
                null
        );
    }

    private record RatingAggregate(double average, int count) {
    }
}
