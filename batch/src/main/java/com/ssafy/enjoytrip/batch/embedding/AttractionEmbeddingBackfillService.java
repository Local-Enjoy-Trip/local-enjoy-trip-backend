package com.ssafy.enjoytrip.batch.embedding;

import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingBackfillReport;
import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingFailure;
import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingGatewayException;
import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingResult;
import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingSource;
import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingTargetRegion;
import com.ssafy.enjoytrip.batch.embedding.AttractionKeywordExpansion;
import com.ssafy.enjoytrip.batch.embedding.gms.GmsAttractionEmbeddingGateway;
import com.ssafy.enjoytrip.batch.embedding.gms.GmsAttractionKeywordExpansionGateway;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionEmbeddingMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionEmbeddingMapper.TargetRegionRow;
import com.ssafy.enjoytrip.storage.db.core.mybatis.row.AttractionEmbeddingSourceRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttractionEmbeddingBackfillService {
    private static final int FAILURE_MESSAGE_LIMIT = 1_000;
    private final GmsAttractionKeywordExpansionGateway keywordExpansionGateway;
    private final GmsAttractionEmbeddingGateway gateway;

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

private final AttractionEmbeddingMapper attractionEmbeddingMapper;

    private List<AttractionEmbeddingSource> findEmbeddingTargets(
            List<AttractionEmbeddingTargetRegion> targetRegions,
            int limit
    ) {
        return attractionEmbeddingMapper.findTargets(toRows(targetRegions), limit).stream()
                .map(AttractionEmbeddingBackfillService::toSource)
                .toList();
    }

    private boolean isEmbeddedWithSameExpandedSourceInDb(Long attractionId, String sourceVersion, String sourceTextHash) {
        return attractionEmbeddingMapper.existsEmbeddedWithSameSource(attractionId, sourceVersion, sourceTextHash) > 0;
    }

    private void saveEmbeddedResult(
            AttractionEmbeddingSource source,
            String sourceVersion,
            String sourceTextHash,
            String embeddingInput,
            AttractionEmbeddingResult result
    ) {
        attractionEmbeddingMapper.upsertEmbedded(
                source.attractionId(),
                toVectorLiteral(result.embedding()),
                sourceVersion,
                sourceTextHash,
                result.dimension(),
                embeddingInput,
                result.provider(),
                result.model()
        );
    }

    private void saveFailedResult(AttractionEmbeddingSource source, String sourceVersion, String sourceTextHash,
                           AttractionEmbeddingFailure failure) {
        attractionEmbeddingMapper.upsertFailed(
                source.attractionId(),
                sourceVersion,
                sourceTextHash,
                failure.code(),
                failure.message()
        );
    }

    private long countEmbeddingsOutsideTargetRegionsInDb(List<AttractionEmbeddingTargetRegion> targetRegions) {
        return attractionEmbeddingMapper.countOutsideTargetRegions(toRows(targetRegions));
    }

    private static List<TargetRegionRow> toRows(List<AttractionEmbeddingTargetRegion> targetRegions) {
        if (targetRegions == null || targetRegions.isEmpty()) {
            throw new IllegalArgumentException("targetRegions가 필요합니다.");
        }
        return targetRegions.stream()
                .map(region -> new TargetRegionRow(region.sidoCode(), region.gugunCode()))
                .toList();
    }

    private static AttractionEmbeddingSource toSource(AttractionEmbeddingSourceRow row) {
        return new AttractionEmbeddingSource(
                row.attractionId(),
                row.title(),
                row.addr1(),
                row.addr2(),
                row.overview(),
                row.sidoCode(),
                row.gugunCode()
        );
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
