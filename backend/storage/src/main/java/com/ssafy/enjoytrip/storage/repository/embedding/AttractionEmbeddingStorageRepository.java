package com.ssafy.enjoytrip.storage.repository.embedding;

import static com.ssafy.enjoytrip.storage.jooq.tables.Attractions.ATTRACTIONS;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingFailure;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingResult;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingSource;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingTargetRegion;
import com.ssafy.enjoytrip.repository.embedding.AttractionEmbeddingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class AttractionEmbeddingStorageRepository implements AttractionEmbeddingRepository {
    private static final Table<?> ATTRACTION_EMBEDDINGS = table(name("attraction_embeddings"));
    private static final Field<Long> EMBEDDING_ATTRACTION_ID = field(
            name("attraction_embeddings", "attraction_id"),
            Long.class
    );
    private static final Field<String> SOURCE_VERSION = field(name("source_version"), String.class);
    private static final Field<String> SOURCE_TEXT_HASH = field(name("source_text_hash"), String.class);
    private static final Field<String> STATUS = field(name("status"), String.class);

    private final DSLContext dslContext;

    @Override
    public List<AttractionEmbeddingSource> findTargets(
            List<AttractionEmbeddingTargetRegion> targetRegions,
            int limit
    ) {
        Condition targetCondition = targetRegionCondition(targetRegions);
        var query = dslContext.select(
                        ATTRACTIONS.ID,
                        ATTRACTIONS.TITLE,
                        ATTRACTIONS.ADDR1,
                        ATTRACTIONS.ADDR2,
                        ATTRACTIONS.OVERVIEW,
                        ATTRACTIONS.SIDO_CODE,
                        ATTRACTIONS.GUGUN_CODE
                )
                .from(ATTRACTIONS)
                .where(targetCondition)
                .orderBy(ATTRACTIONS.ID.asc());

        if (limit > 0) {
            return query.limit(limit).fetch(record -> new AttractionEmbeddingSource(
                    record.get(ATTRACTIONS.ID),
                    record.get(ATTRACTIONS.TITLE),
                    record.get(ATTRACTIONS.ADDR1),
                    record.get(ATTRACTIONS.ADDR2),
                    record.get(ATTRACTIONS.OVERVIEW),
                    record.get(ATTRACTIONS.SIDO_CODE),
                    record.get(ATTRACTIONS.GUGUN_CODE)
            ));
        }
        return query.fetch(record -> new AttractionEmbeddingSource(
                record.get(ATTRACTIONS.ID),
                record.get(ATTRACTIONS.TITLE),
                record.get(ATTRACTIONS.ADDR1),
                record.get(ATTRACTIONS.ADDR2),
                record.get(ATTRACTIONS.OVERVIEW),
                record.get(ATTRACTIONS.SIDO_CODE),
                record.get(ATTRACTIONS.GUGUN_CODE)
        ));
    }

    @Override
    public boolean isEmbeddedWithSameSource(Long attractionId, String sourceVersion, String sourceTextHash) {
        return dslContext.fetchExists(
                dslContext.selectOne()
                        .from(ATTRACTION_EMBEDDINGS)
                        .where(EMBEDDING_ATTRACTION_ID.eq(attractionId))
                        .and(SOURCE_VERSION.eq(sourceVersion))
                        .and(SOURCE_TEXT_HASH.eq(sourceTextHash))
                        .and(STATUS.eq("EMBEDDED"))
        );
    }

    @Override
    @Transactional
    public void saveEmbedded(
            AttractionEmbeddingSource source,
            String sourceVersion,
            String sourceTextHash,
            String embeddingInput,
            AttractionEmbeddingResult result
    ) {
        String vectorLiteral = toVectorLiteral(result.embedding());
        dslContext.query("""
                insert into attraction_embeddings (
                    attraction_id, embedding, source_version, source_text_hash, embedding_dimension,
                    embedding_input, provider, model, status, failure_code, failure_message, attempt_count,
                    last_attempted_at, embedded_at, updated_at
                ) values (
                    ?, ?::vector, ?, ?, ?, ?, ?, ?, 'EMBEDDED', null, null, 1,
                    current_timestamp, current_timestamp, current_timestamp
                )
                on conflict (attraction_id) do update set
                    embedding = excluded.embedding,
                    source_version = excluded.source_version,
                    source_text_hash = excluded.source_text_hash,
                    embedding_dimension = excluded.embedding_dimension,
                    embedding_input = excluded.embedding_input,
                    provider = excluded.provider,
                    model = excluded.model,
                    status = 'EMBEDDED',
                    failure_code = null,
                    failure_message = null,
                    attempt_count = attraction_embeddings.attempt_count + 1,
                    last_attempted_at = current_timestamp,
                    embedded_at = current_timestamp,
                    updated_at = current_timestamp
                """,
                source.attractionId(),
                vectorLiteral,
                sourceVersion,
                sourceTextHash,
                result.dimension(),
                embeddingInput,
                result.provider(),
                result.model()
        ).execute();
    }

    @Override
    @Transactional
    public void saveFailed(AttractionEmbeddingSource source, String sourceVersion, String sourceTextHash,
                           AttractionEmbeddingFailure failure) {
        dslContext.query("""
                insert into attraction_embeddings (
                    attraction_id, source_version, source_text_hash, embedding_dimension,
                    provider, model, status, failure_code, failure_message, attempt_count,
                    last_attempted_at, updated_at
                ) values (
                    ?, ?, ?, 3072, 'gms', 'text-embedding-3-large', 'FAILED', ?, ?, 1,
                    current_timestamp, current_timestamp
                )
                on conflict (attraction_id) do update set
                    source_version = excluded.source_version,
                    source_text_hash = excluded.source_text_hash,
                    embedding_dimension = 3072,
                    provider = 'gms',
                    model = 'text-embedding-3-large',
                    status = 'FAILED',
                    embedding = null,
                    failure_code = excluded.failure_code,
                    failure_message = excluded.failure_message,
                    attempt_count = attraction_embeddings.attempt_count + 1,
                    last_attempted_at = current_timestamp,
                    embedded_at = null,
                    updated_at = current_timestamp
                """,
                source.attractionId(), sourceVersion, sourceTextHash, failure.code(), failure.message()
        ).execute();
    }

    @Override
    public long countEmbeddingsOutsideTargetRegions(List<AttractionEmbeddingTargetRegion> targetRegions) {
        Condition targetCondition = targetRegionCondition(targetRegions);
        return dslContext.selectCount()
                .from(ATTRACTION_EMBEDDINGS)
                .join(ATTRACTIONS).on(EMBEDDING_ATTRACTION_ID.eq(ATTRACTIONS.ID))
                .where(targetCondition.not())
                .fetchOne(0, long.class);
    }

    private static Condition targetRegionCondition(List<AttractionEmbeddingTargetRegion> targetRegions) {
        if (targetRegions == null || targetRegions.isEmpty()) {
            throw new IllegalArgumentException("targetRegions are required");
        }
        Condition condition = null;
        for (AttractionEmbeddingTargetRegion region : targetRegions) {
            Condition regionCondition = ATTRACTIONS.SIDO_CODE.eq(region.sidoCode())
                    .and(ATTRACTIONS.GUGUN_CODE.eq(region.gugunCode()));
            condition = condition == null ? regionCondition : condition.or(regionCondition);
        }
        return condition;
    }

    private static String toVectorLiteral(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException("embedding must not be empty");
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            Double value = embedding.get(i);
            if (value == null || !Double.isFinite(value)) {
                throw new IllegalArgumentException("embedding contains non-finite value at index " + i);
            }
            if (i > 0) {
                builder.append(',');
            }
            builder.append(value);
        }
        return builder.append(']').toString();
    }
}
