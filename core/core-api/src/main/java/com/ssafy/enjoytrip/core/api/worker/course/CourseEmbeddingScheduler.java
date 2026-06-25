package com.ssafy.enjoytrip.core.api.worker.course;

import com.ssafy.enjoytrip.core.domain.CourseEmbeddingProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseEmbeddingScheduler {

    @Value("${enjoytrip.course.embedding.batch-limit:20}")
    private int batchLimit;

    private final CourseEmbeddingProcessor courseEmbeddingProcessor;

    @Scheduled(fixedDelayString = "${enjoytrip.course.embedding.flush-delay-ms:300000}")
    public void embedPendingCourses() {
        List<String> courseIds = courseEmbeddingProcessor.claimPendingBatch(batchLimit);
        if (courseIds.isEmpty()) {
            return;
        }

        log.info("코스 임베딩 배치 시작 - count={}", courseIds.size());
        courseIds.forEach(courseEmbeddingProcessor::processOne);
    }
}
