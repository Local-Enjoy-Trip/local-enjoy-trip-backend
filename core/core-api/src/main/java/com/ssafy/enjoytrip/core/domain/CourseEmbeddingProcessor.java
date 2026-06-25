package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.external.courseembedding.CourseEmbeddingClient;
import com.ssafy.enjoytrip.external.courseembedding.CourseEmbeddingException;
import com.ssafy.enjoytrip.external.courseembedding.CourseEmbeddingInput;
import com.ssafy.enjoytrip.external.courseembedding.CourseEmbeddingResult;
import com.ssafy.enjoytrip.storage.db.core.model.CourseEmbeddingInputRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseEmbeddingMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseEmbeddingProcessor {

    private static final String SOURCE_VERSION = "v1";
    private static final int FAILURE_MESSAGE_LIMIT = 1_000;

    private final CourseEmbeddingMapper courseEmbeddingMapper;
    private final CourseEmbeddingClient courseEmbeddingClient;

    public List<String> claimPendingBatch(int limit) {
        return courseEmbeddingMapper.claimPendingBatch(limit);
    }

    public void processOne(String courseId) {
        CourseEmbeddingInputRecord record =
                courseEmbeddingMapper.findCourseEmbeddingInputById(courseId);
        if (record == null) {
            log.debug("코스 임베딩 건너뜀 - 코스 없음, courseId={}", courseId);
            return;
        }
        if (record.getStopTitles() == null || record.getStopTitles().isBlank()) {
            log.debug("코스 임베딩 건너뜀 - 경유지 없음, courseId={}", courseId);
            return;
        }

        CourseEmbeddingInput input = toInput(record);
        String sourceHash = computeSourceHash(input);

        if (hashUnchanged(courseId, sourceHash)) {
            log.debug("코스 임베딩 건너뜀 - 변경 없음, courseId={}", courseId);
            saveUnchanged(courseId, record.getDominantCategory(), sourceHash);
            return;
        }

        callAndSave(courseId, record.getDominantCategory(), input, sourceHash);
    }

    private static CourseEmbeddingInput toInput(CourseEmbeddingInputRecord record) {
        return new CourseEmbeddingInput(
                record.getCourseId(),
                record.getTitle(),
                record.getRegionName(),
                record.getTagNames(),
                record.getStopTitles()
        );
    }

    private boolean hashUnchanged(String courseId, String sourceHash) {
        return Objects.equals(courseEmbeddingMapper.findSourceHashByCourseId(courseId), sourceHash);
    }

    private void saveUnchanged(String courseId, String dominantCategory, String sourceHash) {
        courseEmbeddingMapper.upsertEmbedded(
                courseId, null, null,
                dominantCategory, SOURCE_VERSION, sourceHash,
                1536, "openai", "unknown"
        );
    }

    private void callAndSave(String courseId, String dominantCategory,
                             CourseEmbeddingInput input, String sourceHash) {
        try {
            CourseEmbeddingResult result = courseEmbeddingClient.embed(input);
            courseEmbeddingMapper.upsertEmbedded(
                    courseId, result.description(), toVectorLiteral(result.embedding()),
                    dominantCategory, SOURCE_VERSION, sourceHash,
                    result.dimension(), result.provider(), result.model()
            );
            log.info("코스 임베딩 완료 - courseId={}", courseId);
        } catch (CourseEmbeddingException ex) {
            log.error("코스 임베딩 실패 - courseId={}, code={}", courseId, ex.failureCode(), ex);
            courseEmbeddingMapper.upsertFailed(
                    courseId, SOURCE_VERSION, sourceHash,
                    "openai", "unknown", ex.failureCode(), limitMessage(ex.getMessage())
            );
        } catch (RuntimeException ex) {
            log.error("코스 임베딩 예기치 않은 실패 - courseId={}", courseId, ex);
            courseEmbeddingMapper.upsertFailed(
                    courseId, SOURCE_VERSION, sourceHash,
                    "openai", "unknown", "COURSE_EMBEDDING_ERROR", limitMessage(ex.getMessage())
            );
        }
    }

    private static String computeSourceHash(CourseEmbeddingInput input) {
        String serialized = nullSafe(input.title()) + "|"
                + nullSafe(input.regionName()) + "|"
                + nullSafe(input.tagNames()) + "|"
                + nullSafe(input.stopTitles());
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

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
