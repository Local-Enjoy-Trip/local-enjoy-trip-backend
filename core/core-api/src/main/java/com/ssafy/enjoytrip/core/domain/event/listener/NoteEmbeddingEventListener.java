package com.ssafy.enjoytrip.core.domain.event.listener;

import com.ssafy.enjoytrip.core.domain.event.NoteEmbeddingRequestedEvent;
import com.ssafy.enjoytrip.external.embedding.NoteEmbeddingClient;
import com.ssafy.enjoytrip.external.embedding.NoteEmbeddingException;
import com.ssafy.enjoytrip.external.embedding.NoteEmbeddingResult;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteEmbeddingMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
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
public class NoteEmbeddingEventListener {
    private static final String SOURCE_VERSION = "v1";
    private static final int FAILURE_MESSAGE_LIMIT = 1_000;

    private final NoteEmbeddingClient noteEmbeddingClient;
    private final NoteEmbeddingMapper noteEmbeddingMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNoteEmbeddingRequested(NoteEmbeddingRequestedEvent event) {
        try {
            NoteEmbeddingResult result = noteEmbeddingClient.embed(event.content());
            noteEmbeddingMapper.upsertEmbedded(
                    event.noteId(),
                    toVectorLiteral(result.embedding()),
                    SOURCE_VERSION,
                    sha256(event.content()),
                    result.dimension(),
                    event.content(),
                    result.provider(),
                    result.model()
            );
            log.info("노트 임베딩 저장 완료 - noteId: {}", event.noteId());
        } catch (NoteEmbeddingException ex) {
            log.error("노트 임베딩 실패 - noteId: {}, code: {}, message: {}",
                    event.noteId(), ex.failureCode(), ex.getMessage());
            noteEmbeddingMapper.upsertFailed(
                    event.noteId(),
                    SOURCE_VERSION,
                    sha256(event.content()),
                    "openai",
                    "unknown",
                    ex.failureCode(),
                    limitMessage(ex.getMessage())
            );
        } catch (RuntimeException ex) {
            log.error("노트 임베딩 예기치 않은 실패 - noteId: {}", event.noteId(), ex);
            noteEmbeddingMapper.upsertFailed(
                    event.noteId(),
                    SOURCE_VERSION,
                    sha256(event.content()),
                    "openai",
                    "unknown",
                    "NOTE_EMBEDDING_ERROR",
                    limitMessage(ex.getMessage())
            );
        }
    }

    private static String toVectorLiteral(List<Double> embedding) {
        return "[" + embedding.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")) + "]";
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
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
