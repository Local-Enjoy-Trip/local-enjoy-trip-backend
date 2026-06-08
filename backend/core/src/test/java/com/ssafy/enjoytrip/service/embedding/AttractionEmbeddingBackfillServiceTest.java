package com.ssafy.enjoytrip.service.embedding;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingFailure;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingGatewayException;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingResult;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingSource;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingTargetRegion;
import com.ssafy.enjoytrip.domain.embedding.AttractionKeywordExpansion;
import com.ssafy.enjoytrip.repository.embedding.AttractionEmbeddingGateway;
import com.ssafy.enjoytrip.repository.embedding.AttractionEmbeddingRepository;
import com.ssafy.enjoytrip.repository.embedding.AttractionKeywordExpansionGateway;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionEmbeddingBackfillServiceTest {
    private static final AttractionEmbeddingTargetRegion GANGNEUNG = new AttractionEmbeddingTargetRegion(
            "강원특별자치도",
            "강릉시",
            32,
            1,
            "proof"
    );
    private static final AttractionEmbeddingTargetRegion JEONJU = new AttractionEmbeddingTargetRegion(
            "전북특별자치도",
            "전주시",
            37,
            12,
            "proof"
    );

    @Test
    void rerunSkipsAlreadyEmbeddedSameSourceWithoutCallingGateway() {
        FakeRepository repository = new FakeRepository(List.of(source(1L, 32, 1)));
        FakeExpansionGateway expansionGateway = new FakeExpansionGateway();
        FakeGateway gateway = new FakeGateway();
        AttractionEmbeddingBackfillService service = new AttractionEmbeddingBackfillService(
                repository,
                expansionGateway,
                gateway
        );
        service.backfill(List.of(GANGNEUNG), "v1", false, 0);
        gateway.calls = 0;
        expansionGateway.calls = 0;
        repository.embedded.clear();

        var report = service.backfill(List.of(GANGNEUNG), "v1", false, 0);

        assertEquals(1, report.skippedCount());
        assertEquals(1, expansionGateway.calls);
        assertEquals(0, gateway.calls);
        assertTrue(repository.embedded.isEmpty());
    }

    @Test
    void embedsExpandedLocalLetterKeywordsInsteadOfRawAttractionText() {
        FakeRepository repository = new FakeRepository(List.of(source(1L, 37, 12)));
        FakeExpansionGateway expansionGateway = new FakeExpansionGateway();
        expansionGateway.expansion = new AttractionKeywordExpansion(List.of(
                "해질녘 전주천 산책",
                "시장 먹거리",
                "한옥마을 외곽 골목"
        ));
        FakeGateway gateway = new FakeGateway();
        AttractionEmbeddingBackfillService service = new AttractionEmbeddingBackfillService(
                repository,
                expansionGateway,
                gateway
        );

        var report = service.backfill(List.of(JEONJU), "v1", false, 0);

        assertEquals(1, report.embeddedCount());
        assertEquals(1, expansionGateway.calls);
        assertEquals("해질녘 전주천 산책\n시장 먹거리\n한옥마을 외곽 골목", gateway.lastSourceText);
    }

    @Test
    void recordsFailureWithTraceableAttractionIdAndFailureDetail() {
        FakeRepository repository = new FakeRepository(List.of(source(1L, 32, 1)));
        FakeExpansionGateway expansionGateway = new FakeExpansionGateway();
        FakeGateway gateway = new FakeGateway();
        gateway.failure = new AttractionEmbeddingGatewayException("GMS_HTTP_500", "GMS failed");
        AttractionEmbeddingBackfillService service = new AttractionEmbeddingBackfillService(
                repository,
                expansionGateway,
                gateway
        );

        var report = service.backfill(List.of(GANGNEUNG), "v1", false, 0);

        assertEquals(1, report.failedCount());
        assertTrue(repository.failures.containsKey(1L));
        assertEquals("GMS_HTTP_500", repository.failures.get(1L).code());
        assertEquals("GMS failed", repository.failures.get(1L).message());
        assertEquals(1, repository.attempts.get(1L));
    }

    @Test
    void recordsKeywordExpansionFailureBeforeEmbeddingRequest() {
        FakeRepository repository = new FakeRepository(List.of(source(1L, 32, 1)));
        FakeExpansionGateway expansionGateway = new FakeExpansionGateway();
        expansionGateway.failure = new AttractionEmbeddingGatewayException(
                "GMS_CHAT_HTTP_500",
                "GMS chat failed"
        );
        FakeGateway gateway = new FakeGateway();
        AttractionEmbeddingBackfillService service = new AttractionEmbeddingBackfillService(
                repository,
                expansionGateway,
                gateway
        );

        var report = service.backfill(List.of(GANGNEUNG), "v1", false, 0);

        assertEquals(1, report.failedCount());
        assertEquals(0, gateway.calls);
        assertEquals("GMS_CHAT_HTTP_500", repository.failures.get(1L).code());
    }

    @Test
    void successAfterFailureUpdatesSameRowAndClearsFailure() {
        FakeRepository repository = new FakeRepository(List.of(source(1L, 32, 1)));
        repository.failures.put(1L, new AttractionEmbeddingFailure("OLD", "old failure"));
        repository.attempts.put(1L, 1);
        FakeExpansionGateway expansionGateway = new FakeExpansionGateway();
        FakeGateway gateway = new FakeGateway();
        AttractionEmbeddingBackfillService service = new AttractionEmbeddingBackfillService(
                repository,
                expansionGateway,
                gateway
        );

        var report = service.backfill(List.of(GANGNEUNG), "v2", false, 0);

        assertEquals(1, report.embeddedCount());
        assertTrue(repository.embedded.containsKey(1L));
        assertFalse(repository.failures.containsKey(1L));
        assertEquals(2, repository.attempts.get(1L));
    }

    @Test
    void dryRunDoesNotCallGatewayOrWriteRows() {
        FakeRepository repository = new FakeRepository(List.of(source(1L, 32, 1)));
        FakeExpansionGateway expansionGateway = new FakeExpansionGateway();
        FakeGateway gateway = new FakeGateway();
        AttractionEmbeddingBackfillService service = new AttractionEmbeddingBackfillService(
                repository,
                expansionGateway,
                gateway
        );

        var report = service.backfill(List.of(GANGNEUNG), "v1", true, 0);

        assertTrue(report.dryRun());
        assertEquals(1, report.skippedCount());
        assertEquals(0, expansionGateway.calls);
        assertEquals(0, gateway.calls);
        assertTrue(repository.embedded.isEmpty());
        assertTrue(repository.failures.isEmpty());
    }

    @Test
    void nonTargetRowsAreNotWrittenWhenRepositoryAppliesCanonicalRegionSelector() {
        FakeRepository repository = new FakeRepository(List.of(
                source(1L, 32, 1),
                source(2L, 32, 2),
                source(3L, 37, 12)
        ));
        FakeExpansionGateway expansionGateway = new FakeExpansionGateway();
        FakeGateway gateway = new FakeGateway();
        AttractionEmbeddingBackfillService service = new AttractionEmbeddingBackfillService(
                repository,
                expansionGateway,
                gateway
        );

        var report = service.backfill(List.of(GANGNEUNG, JEONJU), "v1", false, 0);

        assertEquals(2, report.embeddedCount());
        assertEquals(java.util.Set.of(1L, 3L), repository.embedded.keySet());
        assertFalse(repository.embedded.containsKey(2L));
        assertEquals(2, gateway.calls);
    }

    private static AttractionEmbeddingSource source(Long id, int sidoCode, int gugunCode) {
        return new AttractionEmbeddingSource(id, "title-" + id, "addr", "", "overview", sidoCode, gugunCode);
    }

    private static final class FakeGateway implements AttractionEmbeddingGateway {
        private int calls;
        private String lastSourceText;
        private RuntimeException failure;

        @Override
        public AttractionEmbeddingResult embed(String sourceText) {
            calls++;
            lastSourceText = sourceText;
            if (failure != null) {
                throw failure;
            }
            return new AttractionEmbeddingResult("fake", "text-embedding-3-large", 3, List.of(0.1, 0.2, 0.3));
        }
    }

    private static final class FakeExpansionGateway implements AttractionKeywordExpansionGateway {
        private int calls;
        private AttractionKeywordExpansion expansion = new AttractionKeywordExpansion(List.of(
                "여름 강릉 바다 산책",
                "따뜻한 커피 공간"
        ));
        private RuntimeException failure;

        @Override
        public AttractionKeywordExpansion expand(String attractionSourceText) {
            calls++;
            if (failure != null) {
                throw failure;
            }
            return expansion;
        }
    }

    private static final class FakeRepository implements AttractionEmbeddingRepository {
        private final List<AttractionEmbeddingSource> rows;
        private final Map<Long, String> embeddedHashes = new HashMap<>();
        private final Map<Long, String> embeddedInputs = new HashMap<>();
        private final Map<Long, AttractionEmbeddingResult> embedded = new HashMap<>();
        private final Map<Long, AttractionEmbeddingFailure> failures = new HashMap<>();
        private final Map<Long, Integer> attempts = new HashMap<>();

        private FakeRepository(List<AttractionEmbeddingSource> rows) {
            this.rows = new ArrayList<>(rows);
        }

        @Override
        public List<AttractionEmbeddingSource> findTargets(
                List<AttractionEmbeddingTargetRegion> targetRegions,
                int limit
        ) {
            List<AttractionEmbeddingSource> selected = rows.stream()
                    .filter(row -> targetRegions.stream().anyMatch(region ->
                            region.sidoCode() == row.sidoCode()
                                    && region.gugunCode() == row.gugunCode()
                    ))
                    .toList();
            if (limit > 0 && selected.size() > limit) {
                return selected.subList(0, limit);
            }
            return selected;
        }

        @Override
        public boolean isEmbeddedWithSameSource(
                Long attractionId,
                String sourceVersion,
                String sourceTextHash
        ) {
            return sourceTextHash.equals(embeddedHashes.get(attractionId));
        }

        @Override
        public void saveEmbedded(
                AttractionEmbeddingSource source,
                String sourceVersion,
                String sourceTextHash,
                String embeddingInput,
                AttractionEmbeddingResult result
        ) {
            embedded.put(source.attractionId(), result);
            embeddedHashes.put(source.attractionId(), sourceTextHash);
            embeddedInputs.put(source.attractionId(), embeddingInput);
            failures.remove(source.attractionId());
            attempts.merge(source.attractionId(), 1, Integer::sum);
        }

        @Override
        public void saveFailed(
                AttractionEmbeddingSource source,
                String sourceVersion,
                String sourceTextHash,
                AttractionEmbeddingFailure failure
        ) {
            failures.put(source.attractionId(), failure);
            attempts.merge(source.attractionId(), 1, Integer::sum);
        }

        @Override
        public long countEmbeddingsOutsideTargetRegions(List<AttractionEmbeddingTargetRegion> targetRegions) {
            return 0;
        }
    }
}
