package com.ssafy.enjoytrip.core.domain.event.listener;

import com.ssafy.enjoytrip.core.domain.event.MemberProfileEmbeddingRefreshRequestedEvent;
import com.ssafy.enjoytrip.external.profile.MemberProfileDescriptionResult;
import com.ssafy.enjoytrip.external.profile.MemberProfileEmbeddingClient;
import com.ssafy.enjoytrip.external.profile.MemberProfileEmbeddingException;
import com.ssafy.enjoytrip.external.profile.MemberProfileInput;
import com.ssafy.enjoytrip.storage.db.core.model.SavedAttractionInputRecord;
import com.ssafy.enjoytrip.storage.db.core.model.SavedNoteInputRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberProfileEmbeddingMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberProfileEmbeddingEventListener {
    private static final String SOURCE_VERSION = "v1";
    private static final int FAILURE_MESSAGE_LIMIT = 1_000;

    private final MemberProfileEmbeddingClient memberProfileEmbeddingClient;
    private final MemberProfileEmbeddingMapper memberProfileEmbeddingMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberProfileEmbeddingRefreshRequestedEvent event) {
        Long memberId = event.memberId();

        List<SavedAttractionInputRecord> attractionRecords =
                memberProfileEmbeddingMapper.findSavedAttractionInputsByMemberId(memberId);
        List<SavedNoteInputRecord> noteRecords =
                memberProfileEmbeddingMapper.findSavedNoteInputsByMemberId(memberId);

        if (attractionRecords.isEmpty() && noteRecords.isEmpty()) {
            log.debug("회원 프로필 임베딩 건너뜀 - 저장된 장소/쪽지 없음, memberId: {}", memberId);
            return;
        }

        MemberProfileInput input = buildInput(attractionRecords, noteRecords);
        String sourceHash = computeSourceHash(input);

        String existingHash = memberProfileEmbeddingMapper.findSourceHashByMemberId(memberId);
        if (Objects.equals(existingHash, sourceHash)) {
            log.debug("회원 프로필 임베딩 건너뜀 - 변경 없음, memberId: {}", memberId);
            return;
        }

        try {
            MemberProfileDescriptionResult result = memberProfileEmbeddingClient.embed(input);
            memberProfileEmbeddingMapper.upsertEmbedded(
                    memberId,
                    result.description(),
                    toVectorLiteral(result.embedding()),
                    SOURCE_VERSION,
                    sourceHash,
                    result.dimension(),
                    result.provider(),
                    result.model()
            );
            log.info("회원 프로필 임베딩 저장 완료 - memberId: {}", memberId);
        } catch (MemberProfileEmbeddingException ex) {
            log.error(
                    "회원 프로필 임베딩 실패 - memberId: {}, code: {}, message: {}",
                    memberId, ex.failureCode(), ex.getMessage()
            );
            memberProfileEmbeddingMapper.upsertFailed(
                    memberId,
                    SOURCE_VERSION,
                    sourceHash,
                    "openai",
                    "unknown",
                    ex.failureCode(),
                    limitMessage(ex.getMessage())
            );
        } catch (RuntimeException ex) {
            log.error("회원 프로필 임베딩 예기치 않은 실패 - memberId: {}", memberId, ex);
            memberProfileEmbeddingMapper.upsertFailed(
                    memberId,
                    SOURCE_VERSION,
                    sourceHash,
                    "openai",
                    "unknown",
                    "MEMBER_PROFILE_EMBEDDING_ERROR",
                    limitMessage(ex.getMessage())
            );
        }
    }

    private static MemberProfileInput buildInput(
            List<SavedAttractionInputRecord> attractionRecords,
            List<SavedNoteInputRecord> noteRecords
    ) {
        List<MemberProfileInput.SavedAttractionItem> attractions = attractionRecords.stream()
                .map(r -> {
                    String address = r.getAddr2() != null && !r.getAddr2().isBlank() ? r.getAddr2() : r.getAddr1();
                    return new MemberProfileInput.SavedAttractionItem(
                            r.getTitle(),
                            address,
                            r.getContentTypeId()
                    );
                })
                .toList();

        List<MemberProfileInput.SavedNoteItem> notes = noteRecords.stream()
                .map(r -> new MemberProfileInput.SavedNoteItem(
                        r.getTitle(),
                        r.getCategory(),
                        r.getTagNames()
                ))
                .toList();

        return new MemberProfileInput(attractions, notes);
    }

    private static String computeSourceHash(MemberProfileInput input) {
        String serialized = input.attractions().stream()
                .map(a -> a.title() + "|" + a.addr1() + "|" + a.contentTypeId())
                .collect(Collectors.joining(","))
                + ";"
                + input.notes().stream()
                .map(n -> n.title() + "|" + n.category() + "|" + n.tagNames())
                .collect(Collectors.joining(","));
        return sha256(serialized);
    }

    private static String toVectorLiteral(List<Double> embedding) {
        return "[" + embedding.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")) + "]";
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(text.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 해시를 사용할 수 없습니다.", ex);
        }
    }

    private static String limitMessage(String message) {
        if (message == null || message.isBlank()) {
            return "실패 메시지가 없습니다.";
        }
        String normalized = message.strip();
        return normalized.length() <= FAILURE_MESSAGE_LIMIT
                ? normalized
                : normalized.substring(0, FAILURE_MESSAGE_LIMIT);
    }
}
