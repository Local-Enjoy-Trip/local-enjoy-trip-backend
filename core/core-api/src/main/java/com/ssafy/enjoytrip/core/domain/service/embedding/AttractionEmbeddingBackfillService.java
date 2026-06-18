package com.ssafy.enjoytrip.core.domain.service.embedding;

import static com.ssafy.enjoytrip.storage.db.core.jooq.tables.Attractions.ATTRACTIONS;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import com.ssafy.enjoytrip.core.domain.embedding.AttractionEmbeddingBackfillReport;
import com.ssafy.enjoytrip.core.domain.embedding.AttractionEmbeddingFailure;
import com.ssafy.enjoytrip.core.domain.embedding.AttractionEmbeddingGatewayException;
import com.ssafy.enjoytrip.core.domain.embedding.AttractionEmbeddingResult;
import com.ssafy.enjoytrip.core.domain.embedding.AttractionEmbeddingSource;
import com.ssafy.enjoytrip.core.domain.embedding.AttractionEmbeddingTargetRegion;
import com.ssafy.enjoytrip.core.domain.embedding.AttractionKeywordExpansion;
import com.ssafy.enjoytrip.core.domain.external.embedding.AttractionEmbeddingGateway;
import com.ssafy.enjoytrip.core.domain.external.embedding.AttractionKeywordExpansionGateway;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttractionEmbeddingBackfillService {
    private static final int FAILURE_MESSAGE_LIMIT = 1_000;
    private final AttractionKeywordExpansionGateway keywordExpansionGateway;
    private final AttractionEmbeddingGateway gateway;

    public AttractionEmbeddingBackfillReport backfill(List<AttractionEmbeddingTargetRegion> targetRegions,
                                                      String sourceVersion,
                                                      boolean dryRun,
                                                      int limit) {
        List<AttractionEmbeddingSource> targets = findEmbeddingTargets(targetRegions, limit);
        BackfillCounter counter = new BackfillCounter(dryRun);

        for (AttractionEmbeddingSource source : targets) {
            backfillTarget(source, sourceVersion, dryRun, counter);
        }

        return counter.toReport(targets.size());
    }

    public long countEmbeddingsOutsideTargetRegions(List<AttractionEmbeddingTargetRegion> targetRegions) {
        return countEmbeddingsOutsideTargetRegionsInDb(targetRegions);
    }

    private String sourceText(AttractionEmbeddingSource source) {
        StringBuilder builder = new StringBuilder();
        append(builder, "title", source.title());
        append(builder, "address", joinAddress(source.addr1(), source.addr2()));
        append(builder, "overview", source.overview());
        append(builder, "sidoCode", source.sidoCode());
        append(builder, "gugunCode", source.gugunCode());
        return builder.toString().strip();
    }

    private void backfillTarget(AttractionEmbeddingSource source,
                                String sourceVersion,
                                boolean dryRun,
                                BackfillCounter counter) {
        String attractionSourceText = sourceText(source);
        if (dryRun) {
            counter.skip();
            return;
        }

        try {
            saveExpandedEmbeddingWhenSourceChanged(source, sourceVersion, attractionSourceText, counter);
        } catch (AttractionEmbeddingGatewayException ex) {
            recordGatewayFailure(source, sourceVersion, attractionSourceText, ex, counter);
        } catch (RuntimeException ex) {
            recordUnexpectedFailure(source, sourceVersion, attractionSourceText, ex, counter);
        }
    }

    private void saveExpandedEmbeddingWhenSourceChanged(AttractionEmbeddingSource source,
                                                        String sourceVersion,
                                                        String attractionSourceText,
                                                        BackfillCounter counter) {
        AttractionKeywordExpansion expansion = keywordExpansionGateway.expand(attractionSourceText);
        String embeddingText = expansion.embeddingText();
        String sourceTextHash = sha256(embeddingText);

        if (alreadyEmbeddedWithSameExpandedSource(source, sourceVersion, sourceTextHash)) {
            counter.skip();
            return;
        }

        saveEmbeddedResult(
                source,
                sourceVersion,
                sourceTextHash,
                embeddingText,
                gateway.embed(embeddingText)
        );
        counter.embed();
    }

    private boolean alreadyEmbeddedWithSameExpandedSource(AttractionEmbeddingSource source,
                                                          String sourceVersion,
                                                          String sourceTextHash) {
        return isEmbeddedWithSameExpandedSourceInDb(
                source.attractionId(),
                sourceVersion,
                sourceTextHash
        );
    }

    private void recordGatewayFailure(AttractionEmbeddingSource source,
                                      String sourceVersion,
                                      String attractionSourceText,
                                      AttractionEmbeddingGatewayException ex,
                                      BackfillCounter counter) {
        recordBackfillFailure(
                source,
                sourceVersion,
                attractionSourceText,
                new AttractionEmbeddingFailure(ex.failureCode(), limitMessage(ex.getMessage())),
                counter
        );
    }

    private void recordUnexpectedFailure(AttractionEmbeddingSource source,
                                         String sourceVersion,
                                         String attractionSourceText,
                                         RuntimeException ex,
                                         BackfillCounter counter) {
        recordBackfillFailure(
                source,
                sourceVersion,
                attractionSourceText,
                new AttractionEmbeddingFailure(
                        "EMBEDDING_BACKFILL_ERROR",
                        limitMessage(ex.getMessage())
                ),
                counter
        );
    }

    private void recordBackfillFailure(AttractionEmbeddingSource source,
                                       String sourceVersion,
                                       String attractionSourceText,
                                       AttractionEmbeddingFailure failure,
                                       BackfillCounter counter) {
        saveFailedResult(source, sourceVersion, sha256(attractionSourceText), failure);
        counter.fail();
    }

    private static void append(StringBuilder builder, String label, Object value) {
        if (value == null) {
            return;
        }
        String text = value.toString().strip();
        if (text.isEmpty()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append('\n');
        }
        builder.append(label).append(": ").append(text);
    }

    private static String joinAddress(String addr1, String addr2) {
        String first = addr1 == null ? "" : addr1.strip();
        String second = addr2 == null ? "" : addr2.strip();
        return (first + " " + second).strip();
    }

    private static String sha256(String sourceText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(sourceText.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 해시를 사용할 수 없습니다.", ex);
        }
    }

    private static String limitMessage(String message) {
        String normalized = message == null || message.isBlank()
                ? "실패 메시지가 없습니다."
                : message.strip();
        if (normalized.length() <= FAILURE_MESSAGE_LIMIT) {
            return normalized;
        }
        return normalized.substring(0, FAILURE_MESSAGE_LIMIT);
    }

    private static final class BackfillCounter {
        private final boolean dryRun;
        private int embedded;
        private int skipped;
        private int failed;

        private BackfillCounter(boolean dryRun) {
            this.dryRun = dryRun;
        }

        private void embed() {
            embedded++;
        }

        private void skip() {
            skipped++;
        }

        private void fail() {
            failed++;
        }

        private AttractionEmbeddingBackfillReport toReport(int totalTargets) {
            return new AttractionEmbeddingBackfillReport(totalTargets, embedded, skipped, failed, dryRun);
        }
    }

private static final Table<?> ATTRACTION_EMBEDDINGS = table(name("attraction_embeddings"));
    private static final Field<Long> EMBEDDING_ATTRACTION_ID = field(
            name("attraction_embeddings", "attraction_id"),
            Long.class
    );
    private static final Field<String> SOURCE_VERSION = field(name("source_version"), String.class);
    private static final Field<String> SOURCE_TEXT_HASH = field(name("source_text_hash"), String.class);
    private static final Field<String> STATUS = field(name("status"), String.class);

    private final DSLContext dslContext;

    private List<AttractionEmbeddingSource> findEmbeddingTargets(
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

    private boolean isEmbeddedWithSameExpandedSourceInDb(Long attractionId, String sourceVersion, String sourceTextHash) {
        return dslContext.fetchExists(
                dslContext.selectOne()
                        .from(ATTRACTION_EMBEDDINGS)
                        .where(EMBEDDING_ATTRACTION_ID.eq(attractionId))
                        .and(SOURCE_VERSION.eq(sourceVersion))
                        .and(SOURCE_TEXT_HASH.eq(sourceTextHash))
                        .and(STATUS.eq("EMBEDDED"))
        );
    }

    private void saveEmbeddedResult(
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

    private void saveFailedResult(AttractionEmbeddingSource source, String sourceVersion, String sourceTextHash,
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

    private long countEmbeddingsOutsideTargetRegionsInDb(List<AttractionEmbeddingTargetRegion> targetRegions) {
        Condition targetCondition = targetRegionCondition(targetRegions);
        return dslContext.selectCount()
                .from(ATTRACTION_EMBEDDINGS)
                .join(ATTRACTIONS).on(EMBEDDING_ATTRACTION_ID.eq(ATTRACTIONS.ID))
                .where(targetCondition.not())
                .fetchOne(0, long.class);
    }

    private static Condition targetRegionCondition(List<AttractionEmbeddingTargetRegion> targetRegions) {
        if (targetRegions == null || targetRegions.isEmpty()) {
            throw new IllegalArgumentException("targetRegions가 필요합니다.");
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
            throw new IllegalArgumentException("embedding은 비어 있으면 안 됩니다.");
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            Double value = embedding.get(i);
            if (value == null || !Double.isFinite(value)) {
                throw new IllegalArgumentException(
                        "embedding에 유한하지 않은 값이 있습니다. index=" + i
                );
            }
            if (i > 0) {
                builder.append(',');
            }
            builder.append(value);
        }
        return builder.append(']').toString();
    }
}
