package com.ssafy.enjoytrip.service.embedding;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingBackfillReport;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingFailure;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingGatewayException;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingSource;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingTargetRegion;
import com.ssafy.enjoytrip.domain.embedding.AttractionKeywordExpansion;
import com.ssafy.enjoytrip.repository.embedding.AttractionEmbeddingGateway;
import com.ssafy.enjoytrip.repository.embedding.AttractionEmbeddingRepository;
import com.ssafy.enjoytrip.repository.embedding.AttractionKeywordExpansionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttractionEmbeddingBackfillService {
    private static final int FAILURE_MESSAGE_LIMIT = 1_000;

    private final AttractionEmbeddingRepository repository;
    private final AttractionKeywordExpansionGateway keywordExpansionGateway;
    private final AttractionEmbeddingGateway gateway;

    public AttractionEmbeddingBackfillReport backfill(List<AttractionEmbeddingTargetRegion> targetRegions,
                                                      String sourceVersion,
                                                      boolean dryRun,
                                                      int limit) {
        List<AttractionEmbeddingSource> targets = repository.findTargets(targetRegions, limit);
        BackfillCounter counter = new BackfillCounter(dryRun);

        for (AttractionEmbeddingSource source : targets) {
            backfillTarget(source, sourceVersion, dryRun, counter);
        }

        return counter.toReport(targets.size());
    }

    public String sourceText(AttractionEmbeddingSource source) {
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

        repository.saveEmbedded(
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
        return repository.isEmbeddedWithSameSource(
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
        repository.saveFailed(source, sourceVersion, sha256(attractionSourceText), failure);
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
}
