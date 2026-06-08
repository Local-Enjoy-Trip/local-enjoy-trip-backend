package com.ssafy.enjoytrip.storage.integration;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingFailure;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingResult;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingSource;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingTargetRegion;
import com.ssafy.enjoytrip.repository.embedding.AttractionEmbeddingRepository;
import com.ssafy.enjoytrip.storage.StorageConfiguration;
import com.ssafy.enjoytrip.storage.testsupport.PostgisTestcontainersConfiguration;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@ActiveProfiles("postgis")
@Tag("postgis")
@SpringBootTest(classes = AttractionEmbeddingStorageRepositoryPostgisTest.TestApplication.class)
class AttractionEmbeddingStorageRepositoryPostgisTest {
    @Autowired
    private DSLContext dslContext;

    @Autowired
    private AttractionEmbeddingRepository repository;

    @Test
    void findTargetsSelectsOnlyRequestedRegionPairs() {
        seedAttraction(10_010L, 32, 1);
        seedAttraction(10_011L, 32, 2);
        seedAttraction(10_012L, 37, 12);

        var targets = repository.findTargets(List.of(
                new AttractionEmbeddingTargetRegion(
                        "강원특별자치도",
                        "강릉시",
                        32,
                        1,
                        "proof"
                ),
                new AttractionEmbeddingTargetRegion(
                        "전북특별자치도",
                        "전주시",
                        37,
                        12,
                        "proof"
                )
        ), 0);

        assertThat(targets)
                .extracting(AttractionEmbeddingSource::attractionId)
                .contains(10_010L, 10_012L)
                .doesNotContain(10_011L);
    }

    @Test
    void saveEmbeddedPersistsPgvectorStatusAndClearsPriorFailure() {
        seedAttraction(10_001L, 32, 1);
        AttractionEmbeddingSource source = source(10_001L, 32, 1);
        repository.saveFailed(
                source,
                "v1",
                "a".repeat(64),
                new AttractionEmbeddingFailure("OLD", "old failure")
        );

        repository.saveEmbedded(
                source,
                "v2",
                "b".repeat(64),
                "해질녘 강릉 바다 산책\n따뜻한 커피 공간",
                new AttractionEmbeddingResult(
                        "gms",
                        "text-embedding-3-large",
                        3072,
                        Collections.nCopies(3072, 0.1)
                )
        );

        var record = dslContext.fetchOne("""
                select status, failure_code, failure_message, source_version, source_text_hash,
                       embedding_dimension, embedding_input, attempt_count, embedding::text as embedding_text
                from attraction_embeddings
                where attraction_id = ?
                """, 10_001L);

        assertThat(record.get("status", String.class)).isEqualTo("EMBEDDED");
        assertThat(record.get("failure_code", String.class)).isNull();
        assertThat(record.get("failure_message", String.class)).isNull();
        assertThat(record.get("source_version", String.class)).isEqualTo("v2");
        assertThat(record.get("source_text_hash", String.class)).isEqualTo("b".repeat(64));
        assertThat(record.get("embedding_dimension", Integer.class)).isEqualTo(3072);
        assertThat(record.get("embedding_input", String.class))
                .isEqualTo("해질녘 강릉 바다 산책\n따뜻한 커피 공간");
        assertThat(record.get("attempt_count", Integer.class)).isEqualTo(2);
        assertThat(record.get("embedding_text", String.class)).startsWith("[0.1,0.1");
    }

    @Test
    void saveFailedAfterPriorSuccessClearsStalePgvectorAndEmbeddedAt() {
        seedAttraction(10_002L, 37, 12);
        AttractionEmbeddingSource source = source(10_002L, 37, 12);
        repository.saveEmbedded(
                source,
                "v1",
                "c".repeat(64),
                "해질녘 전주천 산책\n시장 먹거리",
                new AttractionEmbeddingResult(
                        "gms",
                        "text-embedding-3-large",
                        3072,
                        Collections.nCopies(3072, 0.2)
                )
        );

        repository.saveFailed(
                source,
                "v2",
                "d".repeat(64),
                new AttractionEmbeddingFailure("GMS_HTTP_500", "failed")
        );

        var record = dslContext.fetchOne("""
                select status, failure_code, source_version, source_text_hash,
                       embedding::text as embedding_text, embedded_at, attempt_count
                from attraction_embeddings
                where attraction_id = ?
                """, 10_002L);

        assertThat(record.get("status", String.class)).isEqualTo("FAILED");
        assertThat(record.get("failure_code", String.class)).isEqualTo("GMS_HTTP_500");
        assertThat(record.get("source_version", String.class)).isEqualTo("v2");
        assertThat(record.get("source_text_hash", String.class)).isEqualTo("d".repeat(64));
        assertThat(record.get("embedding_text", String.class)).isNull();
        assertThat(record.get("embedded_at")).isNull();
        assertThat(record.get("attempt_count", Integer.class)).isEqualTo(2);
    }

    @Test
    void pgvectorCosineDistanceRanksNearestEmbeddingFirst() {
        long nearAttractionId = 10_020L;
        long farAttractionId = 10_021L;
        seedAttraction(nearAttractionId, 37, 12);
        seedAttraction(farAttractionId, 37, 12);

        repository.saveEmbedded(
                source(nearAttractionId, 37, 12),
                "semantic-test",
                "e".repeat(64),
                "해질녘 전주천 산책\n전주 야간 산책",
                new AttractionEmbeddingResult(
                        "test",
                        "text-embedding-3-large",
                        3072,
                        unitVector(0)
                )
        );
        repository.saveEmbedded(
                source(farAttractionId, 37, 12),
                "semantic-test",
                "f".repeat(64),
                "강릉 커피 공간\n강릉 바다 산책",
                new AttractionEmbeddingResult(
                        "test",
                        "text-embedding-3-large",
                        3072,
                        unitVector(1)
                )
        );

        String queryVector = vectorLiteral(unitVector(0));
        var results = dslContext.fetch("""
                select attraction_id, embedding <=> ?::vector as distance
                from attraction_embeddings
                where status = 'EMBEDDED'
                  and attraction_id in (?, ?)
                order by embedding <=> ?::vector
                """, queryVector, nearAttractionId, farAttractionId, queryVector);

        assertThat(results)
                .extracting(record -> record.get("attraction_id", Long.class))
                .containsExactly(nearAttractionId, farAttractionId);
        assertThat(results.get(0).get("distance", Double.class))
                .isCloseTo(0.0, within(0.0001));
        assertThat(results.get(1).get("distance", Double.class))
                .isCloseTo(1.0, within(0.0001));
    }

    private void seedAttraction(long id, int sidoCode, int gugunCode) {
        dslContext.query("""
                insert into attractions (id, title, addr1, addr2, sido_code, gugun_code, overview)
                values (?, ?, ?, ?, ?, ?, ?)
                on conflict (id) do nothing
                """, id, "임베딩 테스트 관광지", "강원 강릉시", "", sidoCode, gugunCode, "테스트 개요")
                .execute();
    }

    private static AttractionEmbeddingSource source(long id, int sidoCode, int gugunCode) {
        return new AttractionEmbeddingSource(id, "임베딩 테스트 관광지", "강원 강릉시", "", "테스트 개요", sidoCode, gugunCode);
    }

    private static List<Double> unitVector(int oneIndex) {
        List<Double> vector = new ArrayList<>(Collections.nCopies(3072, 0.0));
        vector.set(oneIndex, 1.0);
        return vector;
    }

    private static String vectorLiteral(List<Double> vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector.get(i));
        }
        return builder.append(']').toString();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({StorageConfiguration.class, PostgisTestcontainersConfiguration.class})
    static class TestApplication {
    }
}
